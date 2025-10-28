package org.tma.intern.concert.service.implementation;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import io.quarkus.mongodb.panache.Panache;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.contract.event.BookingCreated;
import org.tma.intern.common.contract.event.SeatInfo;
import org.tma.intern.common.exception.AppException;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.type.Action;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.common.type.SeatType;
import org.tma.intern.concert.model.Seat;
import org.tma.intern.concert.dto.ConcertMapper;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.dto.ConcertResponse;
import org.tma.intern.concert.repository.SeatRepository;
import org.tma.intern.concert.service.SeatService;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BaseSeatService extends BaseService implements SeatService {

    SeatRepository seatRepository;

    ConcertMapper concertMapper;

    @NonFinal
    @Inject
    @Channel("booking.created-out")
    private Emitter<BookingCreated> bookingCreatedEventBus;

    record SeatLayout(SeatType type, int rows, int seatsPerRow, double price) {
    }

    @Override
    public Uni<Boolean> checkAllStatusMatchById(SeatStatus status, List<String> ids, String concertId) {
        return findAllByIds(ids, concertId)
            .map(seats ->
                seats.stream().allMatch(seat -> seat.getStatus() == status)
            );
    }

    @Override
    public Uni<List<String>> generateForConcert(ObjectId concertId) {
        List<Seat> seats = buildSeatsFromLayouts(concertId, predefinedSeatLayouts());
        return seatRepository.persist(seats)
            .replaceWith(
                seats.stream().map(seat ->
                    seat.getId().toHexString()
                ).toList()
            );
    }

    @Override
    public Uni<List<String>> hold(ConcertRequest.SeatIds body, String concertId) {
        return checkAllStatusMatchById(SeatStatus.AVAILABLE, body.ids(), concertId)
            .flatMap(
                result -> {
                    if (result) {
                        var uniActionCombine = UniActionCombine.<List<String>>builder()
                            .uni(
                                updateStatus(
                                    SeatStatus.HELD,
                                    body.ids().stream().map(StringHelper::safeParse).toList(),
                                    concertId,
                                    true
                                )
                            )
                            .action(Action.UPDATE)
                            .entityType(Seat.class)
                            .build();
                        return super.assertActionFail(uniActionCombine);

                    } else {
                        return Uni.createFrom().failure(() ->
                            new AppException(
                                AppError.Failure.Action,
                                new RuntimeException("Seats have been hold"),
                                Response.Status.CONFLICT,
                                "Hold",
                                Seat.class.getSimpleName()
                            )
                        );
                    }
                }
            );
    }

    @Override
    public Uni<List<String>> book(ConcertRequest.SeatIds body, ConcertResponse.Preview concert) {
        return findAllByIds(body.ids(), concert.getId()).map(seats -> {
                    if (!seats.stream().allMatch(seat -> seat.getStatus() == SeatStatus.HELD)) {
                        throw new AppException(
                            AppError.Failure.Action,
                            new RuntimeException("Seats have been booked or you must hold first"),
                            Response.Status.CONFLICT,
                            "Book",
                            Seat.class.getSimpleName()
                        );
                    }
                    return seats;
                }
            )
            .flatMap(seats -> {
                    var uniActionCombine = UniActionCombine.<List<String>>builder()
                        .uni(
                            updateStatus(
                                SeatStatus.BOOKED,
                                seats.stream().map(Seat::getId).toList(),
                                concert.getId(),
                                false
                            )
                        )
                        .action(Action.UPDATE)
                        .entityType(Seat.class)
                        .build();

                    return super.assertActionFail(uniActionCombine).call(
                        () -> sendBookingCreatedEvent(concert, seats)
                    );
                }
            );
    }

    @Override
    public Uni<List<String>> updateStatus(
        SeatStatus before,
        SeatStatus after,
        List<String> seatIds,
        String concertId
    ) {
        return checkAllStatusMatchById(
            before,
            seatIds,
            concertId
        ).flatMap(
            result -> {
                if (result) {
                    return updateStatus(
                        after,
                        seatIds.stream().map(StringHelper::safeParse).toList(),
                        concertId,
                        false
                    );

                } else {
                    return Uni.createFrom().failure(() ->
                        new RuntimeException("Seats are already " + after.name().toLowerCase())
                    );
                }
            }
        );
    }

    @Override
    public Uni<Long> release(Duration timeout) {
        return seatRepository.update(
            Updates.combine(
                Updates.set(Seat.FIELD_STATUS, SeatStatus.AVAILABLE),
                Updates.unset(Seat.FIELD_HELD_AT),
                Updates.unset(Seat.FIELD_HELD_BY)

            )
        ).where(
            Filters.and(
                Filters.eq(Seat.FIELD_STATUS, SeatStatus.HELD),
                Filters.lt(Seat.FIELD_HELD_AT, Instant.now().minus(timeout))
            )
        );
    }

    @Override
    public Uni<List<ConcertResponse.PreviewSeat>> findByConcertId(String concertId) {
        return seatRepository.find(
            Filters.and(
                Filters.eq(Seat.FIELD_CONCERT_ID, concertId)
            ),
            Indexes.compoundIndex(
                Indexes.descending(Seat.FIELD_PRICE),
                Indexes.descending(Seat.FIELD_STATUS)
            )
        ).project(ConcertResponse.PreviewSeat.class).list();
    }

    @Override
    public Uni<List<Seat>> findAllByIds(List<String> ids, String concertId) {
        String nativeQuery = MessageFormat.format(
            "{0} in ?1 and {1} = ?2",
            Seat.FIELD_ID,
            Seat.FIELD_CONCERT_ID
        );

        return seatRepository.find(
            nativeQuery,
            ids.stream().map(StringHelper::safeParse).toList(),
            concertId
        ).list();
    }

    /* --------- Private methods for [CREATE] --------- */
    private Uni<Void> sendBookingCreatedEvent(ConcertResponse.Preview concert, List<Seat> items) {
        BookingCreated event = BookingCreated.newBuilder()
            .setConcertId(concert.getId())
            .setConcertOwnerId(concert.getOwnerId())
            .setCreatedBy(identityContext.getPrincipleName())
            .setItems(items.stream().map(seat -> SeatInfo.newBuilder()
                .setId(seat.getId().toHexString())
                .setCode(seat.getCode())
                .setPrice(seat.getPrice())
                .build()).toList())
            .build();

        return super.commitSentEvent(bookingCreatedEventBus, BookingCreated.class, event);
    }

    private List<SeatLayout> predefinedSeatLayouts() {
        return List.of(
            new SeatLayout(SeatType.VIP, 3, 8, SeatType.VIP.price),
            new SeatLayout(SeatType.VIP, 2, 10, SeatType.VIP.price),
            new SeatLayout(SeatType.STANDARD, 6, 12, SeatType.STANDARD.price),
            new SeatLayout(SeatType.STANDARD, 5, 14, SeatType.STANDARD.price)
        );
    }

    private List<Seat> buildSeatsFromLayouts(ObjectId concertId, List<SeatLayout> layouts) {
        List<Seat> seats = new ArrayList<>();
        for (SeatLayout layout : layouts)
            for (int row = 1; row <= layout.rows(); row++)
                for (int num = 1; num <= layout.seatsPerRow(); num++)
                    seats.add(Seat.builder()
                        .id(new ObjectId())
                        .concertId(concertId.toHexString())
                        .code(layout.type() + "-R" + row + "-S" + num)
                        .type(layout.type())
                        .price(layout.price())
                        .build());
        return seats;
    }

    /* --------- Private methods for [UPDATE] --------- */
    private Uni<List<String>> updateStatus(SeatStatus status, List<ObjectId> ids, String concertId, boolean isHolding) {
        var updateCondition = isHolding ? Updates.combine(
            Updates.set(Seat.FIELD_STATUS, status),
            Updates.set(Seat.FIELD_HELD_AT, Instant.now()),
            Updates.set(Seat.FIELD_HELD_BY, identityContext.getClaim("sub"))
        ) : Updates.combine(
            Updates.set(Seat.FIELD_STATUS, status),
            Updates.unset(Seat.FIELD_HELD_AT),
            Updates.unset(Seat.FIELD_HELD_BY)
        );

        return Panache.getSession(Seat.class).withTransaction(() ->
            Multi.createFrom().iterable(ids)
                .onItem().transformToUniAndMerge(id ->
                    seatRepository.update(updateCondition).where(
                        Filters.and(
                            Filters.eq(Seat.FIELD_CONCERT_ID, concertId),
                            Filters.eq(Seat.FIELD_ID, id)
                        )
                    ).map(updatedCount -> updatedCount > 0 ? id.toHexString() : null)
                ).collect().asList()
        );

    }

}

package org.tma.intern.concert.service.impl;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.common.type.SeatType;
import org.tma.intern.concert.data.Seat;
import org.tma.intern.concert.dto.ConcertMapper;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.dto.ConcertResponse;
import org.tma.intern.concert.repository.SeatRepository;
import org.tma.intern.concert.service.SeatService;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatServiceImpl extends BaseService implements SeatService {

    SeatRepository seatRepository;

    ConcertMapper concertMapper;

    record SeatLayout(SeatType type, int rows, int seatsPerRow, double price) {
    }

    @Override
    public Uni<Boolean> checkAllStatusMatchById(SeatStatus status, List<String> ids, String concertId) {
        return findSeatsById(ids.stream().map(StringHelper::safeParse).toList(), concertId).map(seats ->
            seats.stream().allMatch(seat -> seat.getStatus() == status)
        );
    }

    @Override
    public Uni<List<String>> generateForConcert(ObjectId concertId) {
        List<Seat> seats = buildSeatsFromLayouts(concertId, predefinedSeatLayouts());
        return seatRepository.persist(seats)
            .replaceWith(seats.stream().map(seat -> seat.getId().toHexString()).toList())
            .onFailure().transform(error ->
                new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Create", "Seats")
            );
    }

    @Override
    public Uni<List<String>> hold(ConcertRequest.SeatIds body, String concertId) {
        return checkAllStatusMatchById(SeatStatus.AVAILABLE, body.ids(), concertId).flatMap(
            result -> {
                if (result)
                    return updateStatus(SeatStatus.HELD, body.ids().stream().map(StringHelper::safeParse).toList(), concertId, true)
                        .onFailure().transform(error ->
                            new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Update", "seats status")
                        );
                else return Uni.createFrom().failure(() ->
                    new HttpException(AppError.ACTION_FAILED, Response.Status.CONFLICT,
                        new RuntimeException("Seats have been hold"), "Hold", "seats")
                );

            }
        );
    }

    @Override
    public Uni<List<String>> book(ConcertRequest.SeatIds body, String concertId) {
        return checkAllStatusMatchById(SeatStatus.HELD, body.ids(), concertId).flatMap(
            result -> {
                if (result)
                    return updateStatus(SeatStatus.BOOKED, body.ids().stream().map(StringHelper::safeParse).toList(), concertId, false)
                        .onFailure().transform(error ->
                            new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Update", "seats status")
                        );
                else return Uni.createFrom().failure(() ->
                    new HttpException(AppError.ACTION_FAILED, Response.Status.CONFLICT,
                        new RuntimeException("Seats have been booked or you must hold first"), "Hold", "seats")
                );

            }
        );
    }

    @Override
    public Uni<List<String>> cancel(ConcertRequest.SeatIds body, String concertId) {
        return checkAllStatusMatchById(SeatStatus.BOOKED, body.ids(), concertId).flatMap(
            result -> {
                if (result)
                    return updateStatus(SeatStatus.AVAILABLE, body.ids().stream().map(StringHelper::safeParse).toList(), concertId, false)
                        .onFailure().transform(error ->
                            new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Update", "seats status")
                        );
                else return Uni.createFrom().failure(() ->
                    new HttpException(AppError.ACTION_FAILED, Response.Status.CONFLICT,
                        new RuntimeException("Seats are already available"), "Hold", "seats")
                );

            }
        );
    }

    @Override
    public Uni<Long> release(Duration timeout) {
        return seatRepository.update(Updates.combine(
            Updates.set("status", SeatStatus.AVAILABLE),
            Updates.unset("held_at"),
            Updates.unset("held_by")
        )).where(Filters.and(
            Filters.eq("status", SeatStatus.HELD),
            Filters.lt("held_at", Instant.now().minus(timeout))
        ));
    }

    @Override
    public Uni<List<ConcertResponse.PreviewSeat>> findByConcertId(String concertId) {
        return seatRepository.find(
            Filters.and(
                Filters.eq("concert_id", concertId)/*,
            Filters.gte("price", minPrice), // price >= minPrice && price <= maxPrice
            Filters.lte("price", maxPrice)*/),
            Indexes.ascending("status")
        ).list().map(this::mapToPreviewSeats);
    }

    /* --------- Private methods for [CREATE] --------- */
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
            Updates.set("status", status),
            Updates.set("held_at", Instant.now()),
            Updates.set("held_by", identityContext.getClaim("sub"))
        ) : Updates.combine(
            Updates.set("status", status),
            Updates.unset("held_at"),
            Updates.unset("held_by")
        );
        return Multi.createFrom().iterable(ids).onItem().transformToUniAndMerge(id ->
            seatRepository.update(updateCondition).where(
                Filters.and(
                    Filters.eq("concert_id", concertId),
                    Filters.eq("_id", id)
                )
            ).map(updatedCount -> updatedCount > 0 ? id.toHexString() : null)
        ).collect().asList();
    }

    /* --------- Private methods for [FIND] --------- */
    private Uni<List<Seat>> findSeatsById(List<ObjectId> ids, String concertId) {
        return seatRepository.find("_id in ?1 and concert_id = ?2", ids, concertId).list();
    }

    private List<ConcertResponse.PreviewSeat> mapToPreviewSeats(List<Seat> seats) {
        return seats.stream().map(concertMapper::toPreviewDto).toList();
    }

}

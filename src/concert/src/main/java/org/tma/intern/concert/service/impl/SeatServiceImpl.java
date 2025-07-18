package org.tma.intern.concert.service.impl;

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
import org.tma.intern.common.type.SeatType;
import org.tma.intern.concert.data.Seat;
import org.tma.intern.concert.dto.ConcertMapper;
import org.tma.intern.concert.dto.ConcertResponse;
import org.tma.intern.concert.repository.SeatRepository;
import org.tma.intern.concert.service.SeatService;

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

    @Override
    public Uni<Boolean> isAvailable(String id) {
        return seatRepository.findById(StringHelper.safeParse(id)).map(Seat::isAvailable);
    }

    @Override
    public Uni<List<String>> generateSeatsForConcert(ObjectId concertId) {
        List<Seat> seats = buildSeatsFromLayouts(concertId, predefinedSeatLayouts());
        return seatRepository.persist(seats)
            .replaceWith(seats.stream().map(seat -> seat.getId().toHexString()).toList())
            .onFailure().transform(error ->
                new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Create", "Seats")
            );
    }

    @Override
    public Uni<String> update(String id) {
        return seatRepository.findById(StringHelper.safeParse(id))
            .onItem().ifNull().failWith(() ->
                new HttpException(AppError.RESOURCE_NOT_FOUND, Response.Status.NOT_FOUND, new NullPointerException(), "concert")
            ).flatMap(existingSeat -> {
                existingSeat.setAvailable(!existingSeat.isAvailable());
                updateAuditing(existingSeat);
                return seatRepository.persist(existingSeat).map(saved -> saved.getId().toHexString());
            }).onFailure().transform(error ->
                new HttpException(AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, error, "Approve", "concert")
            );
    }

    @Override
    public Uni<List<ConcertResponse.PreviewSeat>> findByConcertId(String concertId) {
        return seatRepository.find("concert_id", concertId).list().map(this::mapToPreviewSeats);
    }

    record SeatLayout(SeatType type, int rows, int seatsPerRow, double price) {
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
                        .name(layout.type() + "-R" + row + "-S" + num)
                        .type(layout.type())
                        .price(layout.price())
                        .createdBy(identityContext.getClaim("sub"))
                        .build());
        return seats;
    }

    /* --------- Private methods for [UPDATE] --------- */
    public void updateAuditing(Seat seat) {
        seat.setVersion(seat.getVersion() + 1);
        seat.setUpdatedAt(Instant.now());
        seat.setUpdatedBy(identityContext.getClaim("sub"));
    }

    /* --------- Private methods for [FIND] --------- */
    private List<ConcertResponse.PreviewSeat> mapToPreviewSeats(List<Seat> seats) {
        return seats.stream().map(concertMapper::toPreviewDto).toList();
    }

}

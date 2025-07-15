package org.tma.intern.concert.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.tma.intern.common.base.BaseService;
import org.tma.intern.common.dto.SeatType;
import org.tma.intern.common.exception.AppError;
import org.tma.intern.common.exception.HttpException;
import org.tma.intern.concert.data.Seat;
import org.tma.intern.concert.data.SeatRepository;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatServiceImpl extends BaseService implements SeatService {

    private final SeatRepository seatRepository;

    public Uni<List<String>> generateSeatsForConcert(ObjectId concertId) {
        List<SeatLayout> seatLayouts = List.of(
            // VIP sections - luxurious, smaller
            new SeatLayout(SeatType.VIP, 5, 15, SeatType.VIP.price),
            new SeatLayout(SeatType.VIP, 4, 12, SeatType.VIP.price),
            new SeatLayout(SeatType.VIP, 3, 10, SeatType.VIP.price),
            new SeatLayout(SeatType.VIP, 2, 8, SeatType.VIP.price),
            new SeatLayout(SeatType.VIP, 3, 14, SeatType.VIP.price),

            // STANDARD sections - bigger capacity
            new SeatLayout(SeatType.STANDARD, 8, 25, SeatType.STANDARD.price),
            new SeatLayout(SeatType.STANDARD, 10, 20, SeatType.STANDARD.price),
            new SeatLayout(SeatType.STANDARD, 12, 18, SeatType.STANDARD.price),
            new SeatLayout(SeatType.STANDARD, 7, 22, SeatType.STANDARD.price),
            new SeatLayout(SeatType.STANDARD, 9, 24, SeatType.STANDARD.price),
            new SeatLayout(SeatType.STANDARD, 11, 19, SeatType.STANDARD.price),
            new SeatLayout(SeatType.STANDARD, 6, 26, SeatType.STANDARD.price)
        );

        List<Seat> seats = new ArrayList<>();
        for (SeatLayout layout : seatLayouts) {
            for (int row = 1; row <= layout.rows(); row++) {
                for (int num = 1; num <= layout.seatsPerRow(); num++) {
                    seats.add(Seat.builder()
                        .concertId(concertId)
                        .name(layout.type() + "-R" + row + "-S" + num)
                        .type(layout.type())
                        .price(layout.price())
                        .createdBy(identityContext.getClaim("sub"))
                        .build());
                }
            }
        }

        // Persist and return list of IDs as strings
        return seatRepository.persist(seats).replaceWith(seats)
            .onItem().transformToUni(list ->
                Uni.createFrom().item(list.stream().map(seat -> seat.getId().toHexString()).toList()))
            .onFailure().transform(throwable -> new HttpException(
                AppError.ACTION_FAILED, Response.Status.NOT_IMPLEMENTED, throwable, "Create", "Concert"));
    }

    record SeatLayout(SeatType type, int rows, int seatsPerRow, double price) {
    }
}

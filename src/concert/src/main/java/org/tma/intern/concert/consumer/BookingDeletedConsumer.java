package org.tma.intern.concert.consumer;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.tma.intern.common.base.BaseConsumer;
import org.tma.intern.common.contract.event.*;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.concert.model.Seat;
import org.tma.intern.concert.service.ConcertService;
import org.tma.intern.concert.service.SeatService;

import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingDeletedConsumer extends BaseConsumer<BookingDeleted> {

    ConcertService concertService;

    SeatService seatService;

    @NonFinal
    @Inject
    @Channel("rollback.booking.deleted-out")
    private Emitter<RollbackBookingDeleted> rollbackBookingDeletedEventBus;

    @Incoming("booking.deleted-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, BookingDeleted> record) {
        BookingDeleted event = record.getPayload();
        return super.assertActionFailWithRollback(
            deleteBookingWithLogging(event),
            concertService.findById(event.getConcertId()).chain(concert ->
                seatService.findAllByIds(
                        event.getItems().stream().map(SeatId::getValue).toList(),
                        event.getConcertId()
                    )
                    .onItem().transformToUni(seats ->
                        sendRollbackBookingDeletedEvent(
                            event.getUserId(),
                            event.getConcertId(),
                            concert.getOwnerId(),
                            seats
                        )
                    )
            ), record
        );
    }

    private Uni<Void> sendRollbackBookingDeletedEvent(String userId, String concertId, String concertOwnerId, List<Seat> seats) {
        RollbackBookingDeleted event = RollbackBookingDeleted.newBuilder()
            .setUserId(userId)
            .setConcertId(concertId)
            .setConcertOwnerId(concertOwnerId)
            .setDeletedItems(seats.stream().map(seat ->
                SeatInfo.newBuilder()
                    .setId(seat.getId().toHexString())
                    .setCode(seat.getCode())
                    .setPrice(seat.getPrice())
                    .build()).toList()
            ).build();

        return Uni.createFrom().completionStage(() -> rollbackBookingDeletedEventBus.send(event))
            .invoke(() -> log.info("Rollback booking deleted event sent successfully!"));
    }

    private Uni<List<String>> deleteBookingWithLogging(BookingDeleted event) {
        return seatService.updateStatus(
            SeatStatus.BOOKED,
            SeatStatus.AVAILABLE,
            event.getItems().stream().map(SeatId::getValue).toList(),
            event.getConcertId()
        ).invoke(id -> {
            log.warn("Received: concertId: {}", event.getConcertId());
            event.getItems().forEach(seatId -> log.warn("Old seatId: {}", seatId.getValue()));
        });
    }
}

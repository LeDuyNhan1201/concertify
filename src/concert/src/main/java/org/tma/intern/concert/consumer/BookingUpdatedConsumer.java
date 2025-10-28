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
import org.tma.intern.concert.service.SeatService;

import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingUpdatedConsumer extends BaseConsumer<BookingUpdated> {

    SeatService seatService;

    @NonFinal
    @Inject
    @Channel("rollback.booking.updated-out")
    private Emitter<RollbackBookingUpdated> rollbackBookingUpdatedEventBus;

    @Incoming("booking.updated-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, BookingUpdated> record) {
        BookingUpdated event = record.getPayload();
        return super.assertActionFailWithRollback(
            updateBookingWithLogging(event),
            seatService.findAllByIds(
                    event.getOldItems().stream().map(SeatId::getValue).toList(),
                    event.getConcertId()
                )
                .onItem().transformToUni(seats ->
                    sendRollbackBookingUpdatedEvent(
                        event.getConcertId(),
                        event.getBookingId(),
                        seats,
                        event.getNewItems().stream().map(ExistedItem::getId).toList()
                    )
                ),
            record
        );
    }

    private Uni<Void> sendRollbackBookingUpdatedEvent(String concertId, String bookingId, List<Seat> seats, List<String> createdItemIds) {
        RollbackBookingUpdated event = RollbackBookingUpdated.newBuilder()
            .setConcertId(concertId)
            .setBookingId(bookingId)
            .setDeletedItems(seats.stream().map(seat ->
                SeatInfo.newBuilder()
                    .setId(seat.getId().toHexString())
                    .setCode(seat.getCode())
                    .setPrice(seat.getPrice())
                    .build()).toList()
            )
            .setCreatedItems(createdItemIds.stream().map(itemId ->
                ItemId.newBuilder()
                    .setValue(itemId)
                    .build()).toList()
            ).build();

        return Uni.createFrom().completionStage(() -> rollbackBookingUpdatedEventBus.send(event))
            .invoke(() -> log.info("Rollback booking updated event sent successfully!"));
    }

    private Uni<List<String>> updateBookingWithLogging(BookingUpdated event) {
        var uniAction = seatService.updateStatus(
            SeatStatus.AVAILABLE,
            SeatStatus.BOOKED,
            event.getNewItems().stream().map(ExistedItem::getSeatId).toList(),
            event.getConcertId()
        ).invoke(id -> {
            log.warn("BookMore done for bookingId: {}", event.getBookingId());
            event.getNewItems().forEach(existedItem -> log.warn("New seatId: {}", existedItem.getSeatId()));
            // Step 2: Cancel seats
        }).chain(() -> seatService.updateStatus(
                SeatStatus.BOOKED,
                SeatStatus.AVAILABLE,
                event.getOldItems().stream().map(SeatId::getValue).toList(),
                event.getConcertId()
            ).invoke(id -> event.getOldItems().forEach(seatId -> log.warn("Old seatId: {}", seatId.getValue())))

        );

        var uniRollbackAction = seatService.updateStatus(
            SeatStatus.BOOKED,
            SeatStatus.AVAILABLE,
            event.getNewItems().stream().map(ExistedItem::getSeatId).toList(),
            event.getConcertId()
        );

        return super.actionWithRollback(
            uniAction,
            uniRollbackAction
        );

    }

}

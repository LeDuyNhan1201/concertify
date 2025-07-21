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
import org.tma.intern.common.contract.event.*;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.concert.data.Seat;
import org.tma.intern.concert.service.SeatService;

import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingUpdatedConsumer {

    SeatService seatService;

    @NonFinal
    @Inject
    @Channel("rollback.booking.updated-out")
    private Emitter<RollbackBookingUpdated> rollbackBookingUpdatedEventBus;

    static String ERROR_KEY = "booking.updated.error";

    @Incoming("booking.updated-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, BookingUpdated> record) {
        BookingUpdated event = record.getPayload();

        // Step 1: Book more seats
        return seatService.updateStatus(
                SeatStatus.AVAILABLE,
                SeatStatus.BOOKED,
                event.getNewItems().stream().map(ExistedItem::getSeatId).toList(),
                event.getConcertId()
            ).invoke(id -> {
                log.warn("BookMore done for bookingId: {}", event.getBookingId());
                event.getNewItems().forEach(existedItem -> log.warn("New seatId: {}", existedItem.getSeatId()));
            }).chain(() -> seatService.updateStatus(
                        SeatStatus.BOOKED,
                        SeatStatus.AVAILABLE,
                        event.getOldItems().stream().map(SeatId::getValue).toList(),
                        event.getConcertId()
                    ).invoke(id -> event.getOldItems().forEach(seatId -> log.warn("Old seatId: {}", seatId.getValue())))
                    .onFailure().call(throwable -> seatService.updateStatus(
                        SeatStatus.BOOKED,
                        SeatStatus.AVAILABLE,
                        event.getNewItems().stream().map(ExistedItem::getSeatId).toList(),
                        event.getConcertId()
                    )).onItem().failWith(() -> {
                        throw new RuntimeException("Failed to update seat status for bookingId: " + event.getBookingId());
                    })
            ).onItem().transformToUni(v -> Uni.createFrom().completionStage(record.ack()))
            .onFailure().recoverWithUni(error -> {
                // ❌ Có lỗi ở bất kỳ đâu: nack
                log.error("Failed during booking.updated-in processing: {}", error.getMessage(), error);
                return seatService.findSeatsById(event.getOldItems().stream().map(SeatId::getValue).toList(), event.getConcertId())
                    .onItem().transformToUni(seats -> sendRollbackBookingUpdatedEvent(
                        event.getConcertId(),
                        event.getBookingId(),
                        seats,
                        event.getNewItems().stream().map(ExistedItem::getId).toList()
                    )).onItem().transformToUni(id -> Uni.createFrom().completionStage(record.ack()));
//                var metadata = OutgoingKafkaRecordMetadata.<String>builder()
//                    .withKey(ERROR_KEY + ":" + event.getBookingId())
//                    .withHeaders(new RecordHeaders()
//                        .add("error-type", ERROR_KEY.getBytes(StandardCharsets.UTF_8))
//                        .add("error-msg", error.getMessage().getBytes(StandardCharsets.UTF_8)))
//                    .build();
//                return Uni.createFrom().completionStage(record.nack(error, Metadata.of(metadata)));
            });
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
            ).setCreatedItems(createdItemIds.stream().map(itemId ->
                ItemId.newBuilder()
                    .setValue(itemId)
                    .build()).toList()
            ).build();

        return Uni.createFrom().completionStage(() -> rollbackBookingUpdatedEventBus.send(event))
            .invoke(() -> log.info("Rollback booking deleted event sent successfully!"));
    }

}

package org.tma.intern.booking.consumer;

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
import org.tma.intern.booking.service.BookingService;
import org.tma.intern.common.contract.event.BookingCreated;
import org.tma.intern.common.contract.event.RollbackBookingCreated;
import org.tma.intern.common.contract.event.SeatId;
import org.tma.intern.common.contract.event.SeatInfo;

import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingCreatedConsumer {

    BookingService bookingService;

    @NonFinal
    @Inject
    @Channel("rollback.booking.created-out")
    private Emitter<RollbackBookingCreated> rollbackBookingCreatedEventBus;

    @Incoming("booking.created-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, BookingCreated> record) {
        BookingCreated event = record.getPayload();
        return bookingService.create(event)
            .invoke(id -> {
                log.info("Booking created with ID: {}", id);
                log.warn("Received: concertId: {}, concertOwnerId: {}", event.getConcertId(), event.getConcertOwnerId());
                event.getItems().forEach(seatInfo ->
                    log.warn("Received: seatId: {}, price: {}", seatInfo.getId(), seatInfo.getPrice()));
            }).onItem().transformToUni(id -> Uni.createFrom().completionStage(record.ack()))
            .onFailure().recoverWithUni(error -> {
                log.error("Failed to process booking created event: {}", error.getMessage(), error);
                return sendRollbackBookingCreatedEvent(
                    event.getConcertId(),
                    event.getItems()
                ).onItem().transformToUni(id -> Uni.createFrom().completionStage(record.ack()));
//                var metadata = OutgoingKafkaRecordMetadata.<String>builder()
//                    .withKey("booking-failed-" + UUID.randomUUID())
//                    .withHeaders(new RecordHeaders()
//                        .add("error-type", "booking-processing-error".getBytes(StandardCharsets.UTF_8))
//                        .add("error-msg", error.getMessage().getBytes(StandardCharsets.UTF_8)))
//                    .build();
//                return Uni.createFrom().completionStage(record.nack(error, Metadata.of(metadata)));
            });
    }

    private Uni<Void> sendRollbackBookingCreatedEvent(String concertId, List<SeatInfo> seats) {
        RollbackBookingCreated event = RollbackBookingCreated.newBuilder()
            .setConcertId(concertId)
            .setSeatIds(seats.stream().map(seatInfo ->
                SeatId.newBuilder()
                    .setValue(seatInfo.getId())
                    .build()).toList()
            ).build();

        return Uni.createFrom().completionStage(() -> rollbackBookingCreatedEventBus.send(event))
            .invoke(() -> log.info("Rollback booking created event sent successfully!"));
    }

}

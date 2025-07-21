package org.tma.intern.booking.consumer;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.tma.intern.booking.entity.BookingItem;
import org.tma.intern.booking.service.BookingItemService;
import org.tma.intern.booking.service.BookingService;
import org.tma.intern.common.contract.event.BookingCreated;
import org.tma.intern.common.contract.event.RollbackBookingDeleted;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RollbackBookingDeletedConsumer {

    BookingService bookingService;

    BookingItemService bookingItemService;

    static String ERROR_KEY = "rollback.booking.deleted.error";

    @Incoming("rollback.booking.deleted-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, RollbackBookingDeleted> record) {
        RollbackBookingDeleted event = record.getPayload();
        return bookingService.create(
                BookingCreated.newBuilder()
                    .setConcertId(event.getConcertId())
                    .setConcertOwnerId(event.getConcertOwnerId())
                    .build()
            ).invoke(id -> {
                log.warn("Received: bookingId: {}", event.getConcertId());
                event.getDeletedItems().forEach(item -> log.warn("Recovered itemId: {}", item.getId()));
            }).chain(bookingId -> bookingItemService.create(
                        new ObjectId(bookingId),
                        event.getDeletedItems().stream().map(seatInfo ->
                            BookingItem.builder()
                                .seatId(seatInfo.getId())
                                .seatCode(seatInfo.getCode())
                                .price(seatInfo.getPrice())
                                .build()
                        ).toList()
                    ).invoke(createdIds -> createdIds.forEach(deletedId -> log.warn("Deleted itemId: {}", deletedId)))
                    .onFailure().call(throwable -> bookingService.delete(bookingId).onItem().failWith(() -> {
                        throw new RuntimeException("Failed to create items for bookingId: " + bookingId);
                    }))
            ).onItem().transformToUni(id -> Uni.createFrom().completionStage(record.ack()))
            .onFailure().recoverWithUni(error -> {
                log.error("Failed during cancel seats: {}", error.getMessage(), error);
                var metadata = OutgoingKafkaRecordMetadata.<String>builder()
                    .withKey(ERROR_KEY + ":" + UUID.randomUUID())
                    .withHeaders(new RecordHeaders()
                        .add("error-type", ERROR_KEY.getBytes(StandardCharsets.UTF_8))
                        .add("error-msg", error.getMessage().getBytes(StandardCharsets.UTF_8)))
                    .build();
                return Uni.createFrom().completionStage(record.nack(error, Metadata.of(metadata)));
            });
    }

}

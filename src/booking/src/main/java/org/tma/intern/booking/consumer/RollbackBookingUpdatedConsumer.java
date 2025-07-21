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
import org.tma.intern.common.contract.event.ItemId;
import org.tma.intern.common.contract.event.RollbackBookingUpdated;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RollbackBookingUpdatedConsumer {

    BookingItemService bookingItemService;

    static String ERROR_KEY = "rollback.booking.updated.error";

    @Incoming("rollback.booking.updated-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, RollbackBookingUpdated> record) {
        RollbackBookingUpdated event = record.getPayload();
        return bookingItemService.create(
                new ObjectId(event.getBookingId()),
                event.getDeletedItems().stream().map(seatInfo ->
                    BookingItem.builder()
                        .seatId(seatInfo.getId())
                        .seatCode(seatInfo.getCode())
                        .price(seatInfo.getPrice())
                        .build()
                ).toList()
            ).invoke(id -> {
                log.warn("Received: bookingId: {}", event.getConcertId());
                event.getDeletedItems().forEach(item -> log.warn("Recovered itemId: {}", item.getId()));
            }).chain(() -> bookingItemService.delete(event.getCreatedItems().stream().map(ItemId::getValue).toList())
                .invoke(deletedIds -> deletedIds.forEach(deletedId -> log.warn("Deleted itemId: {}", deletedId)))
                .onFailure().call(throwable -> bookingItemService.delete(event.getCreatedItems().stream().map(ItemId::getValue).toList())).onItem().failWith(() -> {
                    throw new RuntimeException("Failed to delete items for bookingId: " + event.getBookingId());
                })).onItem().transformToUni(id -> Uni.createFrom().completionStage(record.ack()))
            .onFailure().recoverWithUni(error -> {
                log.error("Failed during rollback update booking: {}", error.getMessage(), error);
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

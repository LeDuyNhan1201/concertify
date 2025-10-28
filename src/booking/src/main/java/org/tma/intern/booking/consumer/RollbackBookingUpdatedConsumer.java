package org.tma.intern.booking.consumer;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.tma.intern.booking.model.BookingItem;
import org.tma.intern.booking.service.BookingItemService;
import org.tma.intern.common.base.BaseConsumer;
import org.tma.intern.common.contract.event.*;

import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RollbackBookingUpdatedConsumer extends BaseConsumer<RollbackBookingUpdated> {

    @Inject
    BookingItemService bookingItemService;

    static String ERROR_KEY = "rollback.booking.updated.error";

    @Incoming("rollback.booking.updated-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, RollbackBookingUpdated> record) {
        RollbackBookingUpdated event = record.getPayload();
        return super.assertActionFailWithDeadLetter(
            bookingItemService.create(
                new ObjectId(event.getBookingId()),
                event.getDeletedItems().stream().map(seatInfo ->
                    BookingItem.builder()
                        .seatId(seatInfo.getId())
                        .seatCode(seatInfo.getCode())
                        .price(seatInfo.getPrice())
                        .build()
                ).toList()
            ).invoke(itemIds -> {
                log.warn("Received: bookingId: {}", event.getBookingId());
                itemIds.forEach(itemId -> log.warn("Recovered itemId: {}", itemId));
            }).chain(createdIds ->
                super.actionWithRollback(
                    deleteCreatedItems(event.getCreatedItems()),
                    bookingItemService.delete(createdIds)
                )
            ),
            ERROR_KEY,
            record
        );
    }

    private Uni<List<String>> deleteCreatedItems(List<ItemId> createdItemIds) {
        return bookingItemService.delete(
                createdItemIds.stream().map(ItemId::getValue).toList()
            )
            .invoke(deletedIds -> deletedIds.forEach(
                    deletedId -> log.warn("Deleted itemId: {}", deletedId)
                )
            );
    }

}

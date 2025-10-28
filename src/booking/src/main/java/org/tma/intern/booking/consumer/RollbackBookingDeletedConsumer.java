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
import org.tma.intern.booking.service.BookingService;
import org.tma.intern.common.base.BaseConsumer;
import org.tma.intern.common.contract.event.BookingCreated;
import org.tma.intern.common.contract.event.RollbackBookingDeleted;
import org.tma.intern.common.contract.event.SeatInfo;

import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RollbackBookingDeletedConsumer extends BaseConsumer<RollbackBookingDeleted> {

    @Inject
    BookingService bookingService;

    @Inject
    BookingItemService bookingItemService;

    static String ERROR_KEY = "rollback.booking.deleted.error";

    @Incoming("rollback.booking.deleted-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, RollbackBookingDeleted> record) {
        RollbackBookingDeleted event = record.getPayload();
        return super.assertActionFailWithDeadLetter(
            bookingService.create(
                    BookingCreated.newBuilder()
                        .setConcertId(event.getConcertId())
                        .setConcertOwnerId(event.getConcertOwnerId())
                        .build()
                )
                .invoke(concertId -> {
                    log.warn("Received: bookingId: {}", concertId);
                    event.getDeletedItems().forEach(item -> log.warn("Recovered itemId: {}", item.getId()));
                })
                .chain(bookingId ->
                    super.actionWithRollback(
                        createDeletedItems(bookingId, event.getDeletedItems()),
                        bookingService.delete(bookingId)
                    )
                ),
            ERROR_KEY,
            record
        );
    }

    private Uni<List<String>> createDeletedItems(String bookingId, List<SeatInfo> deletedItems) {
        return bookingItemService.create(
                new ObjectId(bookingId),
                deletedItems.stream()
                    .map(seatInfo ->
                        BookingItem.builder()
                            .seatId(seatInfo.getId())
                            .seatCode(seatInfo.getCode())
                            .price(seatInfo.getPrice())
                            .build()
                    ).toList()
            )
            .invoke(createdIds -> createdIds.forEach(
                    deletedId -> log.warn("Deleted itemId: {}", deletedId)
                )
            );
    }

}

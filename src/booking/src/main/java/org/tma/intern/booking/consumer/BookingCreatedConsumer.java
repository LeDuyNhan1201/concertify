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
import org.tma.intern.common.base.BaseConsumer;
import org.tma.intern.common.contract.event.BookingCreated;
import org.tma.intern.common.contract.event.RollbackBookingCreated;
import org.tma.intern.common.contract.event.SeatId;
import org.tma.intern.common.contract.event.SeatInfo;

import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingCreatedConsumer extends BaseConsumer<BookingCreated> {

    final BookingService bookingService;

    @NonFinal
    @Inject
    @Channel("rollback.booking.created-out")
    Emitter<RollbackBookingCreated> rollbackBookingCreatedEventBus;

    @Incoming("booking.created-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, BookingCreated> record) {
        BookingCreated event = record.getPayload();
        return super.assertActionFailWithRollback(
            createBookingWithLogging(bookingService.create(event), event),
            sendRollbackBookingCreatedEvent(
                event.getConcertId(),
                event.getItems()
            ),
            record
        );
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

    private Uni<String> createBookingWithLogging(Uni<String> uniAction, BookingCreated event) {
        return uniAction.invoke(bookingId -> {
            log.warn(
                "Booking created with ID: {}, Received: concertId: {}, concertOwnerId: {}",
                bookingId,
                event.getConcertId(),
                event.getConcertOwnerId()
            );
            event.getItems().forEach(
                seatInfo -> log.warn(
                    "Received: seatId: {}, price: {}",
                    seatInfo.getId(),
                    seatInfo.getPrice()
                )
            );
        });
    }

}

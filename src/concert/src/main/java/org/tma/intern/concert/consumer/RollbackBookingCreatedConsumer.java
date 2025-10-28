package org.tma.intern.concert.consumer;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.tma.intern.common.base.BaseConsumer;
import org.tma.intern.common.contract.event.*;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.concert.service.SeatService;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RollbackBookingCreatedConsumer extends BaseConsumer<RollbackBookingCreated> {

    SeatService seatService;

    static String ERROR_KEY = "rollback.booking.created.error";

    @Incoming("rollback.booking.created-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, RollbackBookingCreated> record) {
        RollbackBookingCreated event = record.getPayload();
        return super.assertActionFailWithDeadLetter(
            seatService.updateStatus(
                SeatStatus.BOOKED,
                SeatStatus.AVAILABLE,
                event.getSeatIds().stream().map(SeatId::getValue).toList(),
                event.getConcertId()
            ).invoke(() -> {
                log.warn("Received: concertId: {}", event.getConcertId());
                event.getSeatIds().forEach(
                    item -> log.warn("seatId: {}", item.getValue()
                    )
                );
            }),
            ERROR_KEY,
            record
        );
    }

}

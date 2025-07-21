package org.tma.intern.concert.consumer;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.tma.intern.common.contract.event.RollbackBookingCreated;
import org.tma.intern.common.contract.event.SeatId;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.concert.service.SeatService;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RollbackBookingCreatedConsumer {

    SeatService seatService;

    static String ERROR_KEY = "rollback.booking.created.error";

    @Incoming("rollback.booking.created-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, RollbackBookingCreated> record) {
        RollbackBookingCreated event = record.getPayload();
        return seatService.updateStatus(
                SeatStatus.BOOKED,
                SeatStatus.AVAILABLE,
                event.getSeatIds().stream().map(SeatId::getValue).toList(),
                event.getConcertId()
            ).invoke(id -> {
                log.warn("Received: concertId: {}", event.getConcertId());
                event.getSeatIds().forEach(item -> log.warn("seatId: {}", item.getValue()));
            }).onItem().transformToUni(id -> Uni.createFrom().completionStage(record.ack()))
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

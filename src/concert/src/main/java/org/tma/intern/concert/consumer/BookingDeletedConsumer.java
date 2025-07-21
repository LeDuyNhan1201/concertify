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
import org.tma.intern.common.contract.event.BookingDeleted;
import org.tma.intern.common.contract.event.RollbackBookingDeleted;
import org.tma.intern.common.contract.event.SeatId;
import org.tma.intern.common.contract.event.SeatInfo;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.concert.data.Seat;
import org.tma.intern.concert.service.ConcertService;
import org.tma.intern.concert.service.SeatService;

import java.util.List;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingDeletedConsumer {

    ConcertService concertService;

    SeatService seatService;

    @NonFinal
    @Inject
    @Channel("rollback.booking.deleted-out")
    private Emitter<RollbackBookingDeleted> rollbackBookingDeletedEventBus;

    static String ERROR_KEY = "booking.deleted.error";

    @Incoming("booking.deleted-in")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> consume(KafkaRecord<String, BookingDeleted> record) {
        BookingDeleted event = record.getPayload();
        return seatService.updateStatus(
                SeatStatus.BOOKED,
                SeatStatus.AVAILABLE,
                event.getItems().stream().map(SeatId::getValue).toList(),
                event.getConcertId()
            ).invoke(id -> {
                log.warn("Received: concertId: {}", event.getConcertId());
                event.getItems().forEach(seatId -> log.warn("Old seatId: {}", seatId.getValue()));
            }).onItem().transformToUni(id -> Uni.createFrom().completionStage(record.ack()))
            .onFailure().recoverWithUni(error -> {
                log.error("Failed during cancel seats: {}", error.getMessage(), error);
                return concertService.findById(event.getConcertId()).chain(concert -> seatService.findSeatsById(event.getItems().stream().map(SeatId::getValue).toList(), event.getConcertId())
                    .onItem().transformToUni(seats -> sendRollbackBookingDeletedEvent(
                        event.getUserId(),
                        event.getConcertId(),
                        concert.getOwnerId(),
                        seats
                    ))
                ).onItem().transformToUni(id -> Uni.createFrom().completionStage(record.ack()));
//                var metadata = OutgoingKafkaRecordMetadata.<String>builder()
//                    .withKey(ERROR_KEY + ":" + event.getBookingId())
//                    .withHeaders(new RecordHeaders()
//                        .add("error-type", ERROR_KEY.getBytes(StandardCharsets.UTF_8))
//                        .add("error-msg", error.getMessage().getBytes(StandardCharsets.UTF_8)))
//                    .build();
//                return Uni.createFrom().completionStage(record.nack(error, Metadata.of(metadata)));
            });
    }

    private Uni<Void> sendRollbackBookingDeletedEvent(String userId, String concertId, String concertOwnerId, List<Seat> seats) {
        RollbackBookingDeleted event = RollbackBookingDeleted.newBuilder()
            .setUserId(userId)
            .setConcertId(concertId)
            .setConcertOwnerId(concertOwnerId)
            .setDeletedItems(seats.stream().map(seat ->
                SeatInfo.newBuilder()
                    .setId(seat.getId().toHexString())
                    .setCode(seat.getCode())
                    .setPrice(seat.getPrice())
                    .build()).toList()
            ).build();

        return Uni.createFrom().completionStage(() -> rollbackBookingDeletedEventBus.send(event))
            .invoke(() -> log.info("Rollback booking deleted event sent successfully!"));
    }

}

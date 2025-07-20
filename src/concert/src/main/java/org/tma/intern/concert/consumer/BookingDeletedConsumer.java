package org.tma.intern.concert.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.tma.intern.common.contract.event.BookingDeleted;
import org.tma.intern.common.contract.event.BookingItemChanged;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.service.SeatService;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingDeletedConsumer {

    SeatService seatService;

    @Incoming("booking.deleted-in")
    public void consume(BookingDeleted event) {
        log.warn("Received: concertId: {}", event.getConcertId());
        event.getItems().forEach(bookingItemCreated -> log.warn("Received: seatId: {}", bookingItemCreated.getSeatId()));

        seatService.cancel(new ConcertRequest.SeatIds(
            event.getItems().stream().map(BookingItemChanged::getSeatId).toList()
        ), event.getConcertId()).onFailure().invoke(throwable ->
            log.error("Failed to process booking deleted event caused by {}", throwable.getMessage(), throwable)
        ).subscribe().with(seatIds -> log.info("Seats cancelled: {}", seatIds));
    }

}

package org.tma.intern.concert.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.tma.intern.common.contract.event.BookingItemChanged;
import org.tma.intern.common.contract.event.BookingUpdated;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.service.SeatService;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingUpdatedConsumer {

    SeatService seatService;

    @Incoming("booking.updated-in")
    public void consume(BookingUpdated event) {
        log.warn("Received: concertId: {}", event.getConcertId());
        event.getOldItems().forEach(bookingItemChanged ->
            log.warn("Received: old seatId: {}", bookingItemChanged.getSeatId()));
        event.getNewItems().forEach(bookingItemChanged ->
            log.warn("Received: new seatId: {}", bookingItemChanged.getSeatId()));
        seatService.bookMore(new ConcertRequest.SeatIds(
                event.getNewItems().stream().map(BookingItemChanged::getSeatId).toList()
            ), event.getConcertId())
            .onFailure().invoke(throwable ->
                log.error("Failed to process booking more items event caused by {}", throwable.getMessage(), throwable)
            ).invoke(seatIds -> log.info("New items: {}", seatIds))
            .chain(() -> seatService.cancel(new ConcertRequest.SeatIds(
                        event.getOldItems().stream().map(BookingItemChanged::getSeatId).toList()
                    ), event.getConcertId())
                    .onFailure().invoke(throwable ->
                        log.error("Failed to process deleting items event caused by {}", throwable.getMessage(), throwable)
                    )
            ).subscribe().with(seatIds -> log.info("Deleted items: {}", seatIds));
    }

}

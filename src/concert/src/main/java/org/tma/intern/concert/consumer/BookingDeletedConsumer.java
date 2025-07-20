package org.tma.intern.concert.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.tma.intern.common.contract.event.BookingCreated;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingDeletedConsumer {

    @Incoming("booking.deleted-in")
    public void consume(BookingCreated event) {
        log.warn("Received: concertId: {}, concertOwnerId: {}", event.getConcertId(), event.getConcertOwnerId());
        event.getItems().forEach(bookingItemCreated ->
            log.warn("Received: seatId: {}, price: {}", bookingItemCreated.getSeatId(), bookingItemCreated.getPrice()));
    }

}

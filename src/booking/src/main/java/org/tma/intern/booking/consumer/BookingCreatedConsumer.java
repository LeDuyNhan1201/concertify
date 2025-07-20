package org.tma.intern.booking.consumer;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.tma.intern.booking.service.BookingService;
import org.tma.intern.common.contract.event.BookingCreated;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingCreatedConsumer {

    BookingService bookingService;

    @Incoming("booking.created-in")
    public void receiveGreeting(BookingCreated event) {
        log.warn("Received: concertId: {}, concertOwnerId: {}", event.getConcertId(), event.getConcertOwnerId());
        event.getItems().forEach(bookingItemCreated ->
            log.warn("Received: seatId: {}, price: {}", bookingItemCreated.getSeatId(), bookingItemCreated.getPrice()));
        bookingService.create(event)
            .onFailure().invoke(throwable ->
                log.error("Failed to process booking created event caused by {}", throwable.getMessage(), throwable)
            ).subscribe().with(id -> log.info("Booking created with ID: {}", id));
    }

}

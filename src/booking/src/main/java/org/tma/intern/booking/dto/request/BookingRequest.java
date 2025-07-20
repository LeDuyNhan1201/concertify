package org.tma.intern.booking.dto.request;

import java.util.List;

public class BookingRequest {

    public record Body(
        String concertId,
        String concertOwnerId,
        List<BookingItemRequest.Body> items
    ) {
    }
    ;

    public record Update(
        List<String> oldItems,
        List<BookingItemRequest.Body> newItems
    ) {
    }
    ;

}

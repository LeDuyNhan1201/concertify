package org.tma.intern.booking.dto;

import java.util.List;

public class BookingRequest {

    public record Body(
        List<BookingItemRequest.Body> items
    ) {
    }

    public record Update(
        List<String> deletedItems,
        List<BookingItemRequest.Body> newItems
    ) {
    }
    ;

}

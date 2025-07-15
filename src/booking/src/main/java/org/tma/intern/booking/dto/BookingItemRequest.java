package org.tma.intern.booking.dto;

public class BookingItemRequest {

    public record Body(
        String seatId,
        long price
    ) {
    }
    ;

}

package org.tma.intern.booking.dto.request;

public class BookingItemRequest {

    public record Body(
        String seatId,
        String seatCode,
        long price
    ) {
    }
    ;

}

package org.tma.intern.booking.dto.request;

public interface BookingItemRequest {

    record Info(

        String seatId,

        String seatCode,

        long price

    ) {
    }

}

package org.tma.intern.booking.dto.request;

import java.util.List;

public interface BookingRequest {

    record Info(

        String concertId,

        String concertOwnerId,

        List<BookingItemRequest.Info> items

    ) {
    }

    record UpdatedInfo(

        List<String> oldItems,

        List<BookingItemRequest.Info> newItems

    ) {
    }

}

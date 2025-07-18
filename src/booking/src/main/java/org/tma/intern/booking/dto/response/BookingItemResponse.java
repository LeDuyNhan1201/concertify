package org.tma.intern.booking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

public class BookingItemResponse {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Detail {
        String id;
        String seatId;
        String seatName;
        double price;
    }

}

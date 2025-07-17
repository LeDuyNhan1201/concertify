package org.tma.intern.booking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.tma.intern.common.type.BookingStatus;

import java.util.List;

public class BookingResponse {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Detail {
        String id;
        String createdBy;
        BookingStatus status;
        List<BookingItemResponse.Detail> items;
    }

}

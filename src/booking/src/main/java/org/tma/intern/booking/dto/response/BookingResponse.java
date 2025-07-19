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
    public static class Details {
        String id;
        String concertId;
        String concertOwnerId;
        String ownerId;
        BookingStatus status;
        List<BookingItemResponse.Detail> items;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Preview {
        String id;
        String concertId;
        String concertOwnerId;
        String ownerId;
        BookingStatus status;
        List<BookingItemResponse.Detail> items;
    }

}

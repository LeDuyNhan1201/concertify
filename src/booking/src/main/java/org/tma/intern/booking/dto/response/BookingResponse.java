package org.tma.intern.booking.dto.response;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.tma.intern.booking.model.Booking;
import org.tma.intern.common.type.BookingStatus;

import java.util.List;

public interface BookingResponse {

    @RegisterForReflection
    @ProjectionFor(Booking.class)
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class Details {

        String id;

        String concertId;

        String concertOwnerId;

        String ownerId;

        BookingStatus status;

        List<BookingItemResponse.Details> items;

    }

    @RegisterForReflection
    @ProjectionFor(Booking.class)
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class Preview {

        String id;

        String concertId;

        String concertOwnerId;

        String ownerId;

        BookingStatus status;

        List<BookingItemResponse.Details> items;

    }

}

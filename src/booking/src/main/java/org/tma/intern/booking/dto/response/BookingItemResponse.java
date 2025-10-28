package org.tma.intern.booking.dto.response;

import io.quarkus.mongodb.panache.common.ProjectionFor;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.tma.intern.booking.model.BookingItem;

public interface BookingItemResponse {

    @RegisterForReflection
    @ProjectionFor(BookingItem.class)
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class Details {

        String id;

        String seatId;

        String seatCode;

        double price;

    }

}

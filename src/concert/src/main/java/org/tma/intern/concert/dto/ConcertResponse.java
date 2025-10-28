package org.tma.intern.concert.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.mongodb.panache.common.ProjectionFor;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.common.type.SeatType;
import org.tma.intern.concert.model.Concert;
import org.tma.intern.concert.model.Seat;

import java.util.List;

public interface ConcertResponse {

    @RegisterForReflection
    @ProjectionFor(Concert.class)
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class Preview {

        String id;

        String title;

        String location;

        String startTime;

        String endTime;

        String ownerId;

        Region region;

    }

    @RegisterForReflection
    @ProjectionFor(Concert.class)
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class PreviewWithSeats {

        String id;

        String title;

        String startTime;

        String endTime;

        String location;

        Region region;

        List<PreviewSeat> seats;

    }


    @RegisterForReflection
    @ProjectionFor(Concert.class)
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class DetailsWithSeats {

        String id;

        String title;

        String startTime;

        String endTime;

        String location;

        String ownerId;

        Region region;

        boolean isApproved;

        List<PreviewSeat> seats;

    }

    @RegisterForReflection
    @ProjectionFor(Seat.class)
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class PreviewSeat {

        String id;

        String code;

        SeatType type;

        double price;

        SeatStatus status;

        String heldBy;

        String heldAt;

    }

}

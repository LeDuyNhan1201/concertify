package org.tma.intern.concert.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.tma.intern.common.type.Region;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.common.type.SeatType;

import java.time.Instant;
import java.util.List;

public class ConcertResponse {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Preview {
        String id;
        String title;
        String location;
        String startTime;
        String endTime;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PreviewWithSeats {
        String id;
        String title;
        String startTime;
        String endTime;
        String location;
        Region region;
        List<PreviewSeat> seats;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class DetailsWithSeats {
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

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PreviewSeat {
        String id;
        String code;
        SeatType type;
        double price;
        SeatStatus status;
        String heldBy;
        String heldAt;
    }

}

package org.tma.intern.concert.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
    public static class Details {
        String id;
        String name;
        String startTime;
        String endTime;
        String location;
        String region;
        boolean isApproved;
    }

}

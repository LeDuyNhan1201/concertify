package org.tma.intern.concert.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface ConcertRequest {

    record Info(

        String title,

        String location,

        LocalDateTime startTime,

        LocalDateTime endTime

    ) {
    }

    record SeatIds(

        List<String> ids

    ) {
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    class SearchQuery {

        String keyword;

        Instant from;

        Instant to;

    }

}

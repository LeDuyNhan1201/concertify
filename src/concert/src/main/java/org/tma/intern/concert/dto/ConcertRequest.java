package org.tma.intern.concert.dto;

import java.time.LocalDateTime;

public class ConcertRequest {

    public record Body(
            String title,
            String description,
            String location,
            LocalDateTime startTime,
            LocalDateTime endTime
    ){};

}

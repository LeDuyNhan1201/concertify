package org.tma.intern.concert.dto;

import org.tma.intern.common.dto.Region;

import java.time.LocalDateTime;

public class ConcertRequest {

    public record Body(
            String title,
            String description,
            String location,
            Region region,
            LocalDateTime startTime,
            LocalDateTime endTime
    ){};

}

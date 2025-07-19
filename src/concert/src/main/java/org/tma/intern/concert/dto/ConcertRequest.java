package org.tma.intern.concert.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ConcertRequest {

    public record Info(
            String title,
            String location,
            LocalDateTime startTime,
            LocalDateTime endTime
    ){};

    public record SeatIds(
        List<String> ids
    ){};

}

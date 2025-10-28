package org.tma.intern.concert.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.tma.intern.concert.service.SeatService;

import java.time.Duration;

@Slf4j
@Singleton
public class SeatCleanupScheduler {

    @Inject
    SeatService seatService;

    @Scheduled(every = "30s")
    public void cleanupExpiredHolds() {
        seatService.release(Duration.ofMinutes(5))
            .subscribe().with(
                count -> log.info("Released {} expired HELD seats", count),
                error -> log.error("Seat cleanup failed caused by {}", error.getMessage(), error)
            );
    }
}

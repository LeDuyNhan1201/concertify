package org.tma.intern.concert;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.concert.repository.SeatRepository;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Singleton
public class SeatCleanupScheduler {

    @Inject
    SeatRepository seatRepository;

    public Uni<Long> release(Duration timeout) {
        return seatRepository.update(Updates.combine(
            Updates.set("status", SeatStatus.AVAILABLE),
            Updates.unset("held_at"),
            Updates.unset("held_by")
        )).where(Filters.and(
            Filters.eq("status", SeatStatus.HELD),
            Filters.lt("held_at", Instant.now().minus(timeout))
        ));
    }

    @Scheduled(every = "30s") // hoặc "1m"
    public void cleanupExpiredHolds() {
        release(Duration.ofMinutes(5))
            .subscribe().with(
                count -> log.info("Released {} expired HELD seats", count),
                error -> log.error("Seat cleanup failed caused by {}", error.getMessage(), error)
            );
    }
}

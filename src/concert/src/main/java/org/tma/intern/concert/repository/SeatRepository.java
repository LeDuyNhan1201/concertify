package org.tma.intern.concert.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.tma.intern.concert.data.Seat;

@ApplicationScoped
public class SeatRepository implements ReactivePanacheMongoRepository<Seat> {

}

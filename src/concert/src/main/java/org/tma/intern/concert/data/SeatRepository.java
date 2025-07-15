package org.tma.intern.concert.data;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SeatRepository implements ReactivePanacheMongoRepository<Seat> {

}

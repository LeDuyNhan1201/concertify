package org.tma.intern.concert.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.tma.intern.concert.data.Concert;

@ApplicationScoped
public class ConcertRepository implements ReactivePanacheMongoRepository<Concert> {

}

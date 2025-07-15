package org.tma.intern.booking.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.tma.intern.booking.entity.Booking;

@ApplicationScoped
public class BookingRepository implements ReactivePanacheMongoRepository<Booking> {

}

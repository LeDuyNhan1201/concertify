package org.tma.intern.booking.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.tma.intern.booking.entity.BookingItem;

@ApplicationScoped
public class BookingItemRepository implements ReactivePanacheMongoRepository<BookingItem> {

}

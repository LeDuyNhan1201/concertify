package org.tma.intern.booking.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.tma.intern.booking.model.BookingItem;

import java.util.List;

public interface BookingItemService {

    Uni<Boolean> isAnyExisted(List<String> seatIds);

    Uni<List<String>> create(ObjectId bookingId, List<BookingItem> items);

    Uni<String> delete(ObjectId id);

    Uni<List<String>> delete(List<String> ids);

    Uni<List<BookingItem>> findAllByBookingId(String bookingId);

    Multi<BookingItem> findByIds(List<String> ids);

}

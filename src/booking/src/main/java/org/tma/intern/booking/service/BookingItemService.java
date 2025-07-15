package org.tma.intern.booking.service;

import io.smallrye.mutiny.Uni;
import org.tma.intern.booking.dto.BookingItemRequest;
import org.tma.intern.booking.dto.BookingItemResponse;

import java.util.List;

public interface BookingItemService {

    Uni<List<String>> create(String bookingId, List<BookingItemRequest.Body> request);

    Uni<String> delete(String id);

    Uni<List<String>> delete(List<String> ids);

    Uni<List<BookingItemResponse.Detail>> findAllByBookingId(String bookingId);

}

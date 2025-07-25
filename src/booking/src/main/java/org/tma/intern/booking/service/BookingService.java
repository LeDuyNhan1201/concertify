package org.tma.intern.booking.service;

import io.smallrye.mutiny.Uni;
import org.tma.intern.booking.dto.request.BookingRequest;
import org.tma.intern.booking.dto.response.BookingResponse;
import org.tma.intern.common.contract.event.BookingCreated;
import org.tma.intern.common.type.BookingStatus;
import org.tma.intern.common.dto.PageResponse;

import java.util.List;

public interface BookingService {

    Uni<String> create(BookingCreated event);

    Uni<String> update(String id, BookingRequest.Update request);

    Uni<String> update(String id, BookingStatus status);

    Uni<String> softDelete(String id);

    Uni<String> delete(String id);

    Uni<BookingResponse.Details> details(String id);

    Uni<BookingResponse.Preview> preview(String id);

    Uni<PageResponse<BookingResponse.Details>> search(int page, int limit);

    Uni<PageResponse<BookingResponse.Details>> bookingsOfMyConcerts(int index, int limit);

    Uni<List<String>> seedData(int count);

}

package org.tma.intern.booking.service;

import io.smallrye.mutiny.Uni;
import org.tma.intern.booking.dto.BookingRequest;
import org.tma.intern.booking.dto.BookingResponse;
import org.tma.intern.common.dto.BookingStatus;
import org.tma.intern.common.dto.PageResponse;

import java.util.List;

public interface BookingService {

    Uni<String> create(BookingRequest.Body request);

    Uni<String> update(String id, BookingRequest.Update request);

    Uni<String> update(String id, BookingStatus status);

    Uni<String> softDelete(String id);

    Uni<String> delete(String id);

    Uni<BookingResponse.Detail> findById(String id);

    Uni<PageResponse<BookingResponse.Detail>> findAll(int page, int limit);

    Uni<List<String>> seedData(int count);

}

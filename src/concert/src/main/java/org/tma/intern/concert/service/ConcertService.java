package org.tma.intern.concert.service;

import io.smallrye.mutiny.Uni;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.common.type.Region;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.dto.ConcertResponse;

import java.util.List;

public interface ConcertService {

    Uni<String> create(ConcertRequest.Info request);

    Uni<String> update(String id, ConcertRequest.Info request);

    Uni<String> approve(String id);

    Uni<String> softDelete(String id);

    Uni<ConcertResponse.Preview> findById(String id);

    Uni<ConcertResponse.PreviewWithSeats> preview(String id);

    Uni<ConcertResponse.DetailsWithSeats> details(String id);

    Uni<PageResponse<ConcertResponse.Preview>> search(ConcertRequest.SearchQuery query, int offset, int limit, boolean isOrganizer);

    Uni<List<String>> seedData(int count, Region region);

}

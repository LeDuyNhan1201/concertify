package org.tma.intern.concert.service;

import io.smallrye.mutiny.Uni;
import org.tma.intern.common.dto.PageResponse;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.dto.ConcertResponse;

import java.util.List;

public interface ConcertService {

    Uni<String> create(ConcertRequest.Body request);

    Uni<String> update(String id, ConcertRequest.Body request);

    Uni<String> approve(String id);

    Uni<String> softDelete(String id);

    Uni<ConcertResponse.Detail> findById(String id);

    Uni<PageResponse<ConcertResponse.Preview>> findAll(int offset, int limit);

    Uni<List<String>> seedData(int count);

}

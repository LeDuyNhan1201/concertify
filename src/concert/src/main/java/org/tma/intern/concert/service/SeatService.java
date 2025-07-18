package org.tma.intern.concert.service;

import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.tma.intern.concert.dto.ConcertResponse;

import java.util.List;

public interface SeatService {

    Uni<Boolean> isAvailable(String id);

    Uni<List<String>> generateSeatsForConcert(ObjectId concertId);

    Uni<String> update(String id);

    Uni<List<ConcertResponse.PreviewSeat>> findByConcertId(String concertId);

}

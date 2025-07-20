package org.tma.intern.concert.service;

import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.concert.dto.ConcertRequest;
import org.tma.intern.concert.dto.ConcertResponse;

import java.time.Duration;
import java.util.List;

public interface SeatService {

    Uni<Boolean> checkAllStatusMatchById(SeatStatus status, List<String> ids, String concertId);

    Uni<List<String>> generateForConcert(ObjectId concertId);

    Uni<List<String>> hold(ConcertRequest.SeatIds body, String concertId);

    Uni<List<String>> book(ConcertRequest.SeatIds body, ConcertResponse.Preview concert);

    Uni<List<String>> bookMore(ConcertRequest.SeatIds body, String concertId);

    Uni<List<String>> cancel(ConcertRequest.SeatIds body, String concertId);

    Uni<Long> release(Duration timeout);

    Uni<List<ConcertResponse.PreviewSeat>> findByConcertId(String concertId);

}

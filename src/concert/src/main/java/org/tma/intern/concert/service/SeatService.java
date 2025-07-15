package org.tma.intern.concert.service;

import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;

import java.util.List;

public interface SeatService {

    Uni<List<String>> generateSeatsForConcert(ObjectId concertId);

}

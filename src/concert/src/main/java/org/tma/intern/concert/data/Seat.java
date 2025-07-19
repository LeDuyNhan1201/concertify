package org.tma.intern.concert.data;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.common.type.SeatType;

import java.time.Instant;

@MongoEntity(database = "concert-service", collection = "seats")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Seat {

    @BsonId
    ObjectId id;

    @BsonProperty(value = "concert_id")
    String concertId;

    @BsonProperty(value = "code")
    String code;

    @BsonProperty(value = "type")
    @Builder.Default
    SeatType type = SeatType.STANDARD;

    @BsonProperty(value = "price")
    @Builder.Default
    double price = SeatType.STANDARD.price;

    @BsonProperty(value = "status")
    @Builder.Default
    SeatStatus status = SeatStatus.AVAILABLE;

    @BsonProperty(value = "held_by")
    String heldBy;

    @BsonProperty(value = "held_at")
    Instant heldAt;

}

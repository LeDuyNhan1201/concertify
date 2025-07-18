package org.tma.intern.concert.data;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.tma.intern.common.base.AuditCollection;
import org.tma.intern.common.type.SeatType;

@MongoEntity(database = "concert-service", collection = "seats")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Seat extends AuditCollection {

    @BsonProperty(value = "concert_id")
    String concertId;

    @BsonProperty(value = "name")
    String name;

    @BsonProperty(value = "type")
    @Builder.Default
    SeatType type = SeatType.STANDARD;

    @BsonProperty(value = "price")
    @Builder.Default
    double price = SeatType.STANDARD.price;

    @BsonProperty(value = "available")
    @Builder.Default
    boolean available = true;

}

package org.tma.intern.booking.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@MongoEntity(database = "booking-service", collection = "booking_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingItem {

    @BsonId
    ObjectId id;

    @BsonProperty(value = "booking_id")
    String bookingId;

    @BsonProperty(value = "seat_id")
    String seatId;

    @BsonProperty(value = "seat_code")
    String seatCode;

    @BsonProperty(value = "price")
    double price;

}

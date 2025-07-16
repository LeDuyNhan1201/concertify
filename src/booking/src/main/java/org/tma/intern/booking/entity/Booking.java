package org.tma.intern.booking.entity;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.tma.intern.common.base.AuditCollection;
import org.tma.intern.common.type.BookingStatus;

@MongoEntity(database = "booking-service", collection = "bookings")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking extends AuditCollection {

    @BsonProperty(value = "concert_id")
    String concertId;

    @BsonProperty(value = "concert_owner_id")
    String concertOwnerId;

    @BsonProperty(value = "status")
    @Builder.Default
    BookingStatus status = BookingStatus.PENDING;

}

package org.tma.intern.booking.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.tma.intern.common.base.AuditCollection;
import org.tma.intern.common.type.BookingStatus;

@MongoEntity(database = "booking-service", collection = Booking.COLLECTION_NAME)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking extends AuditCollection {

    public static final String COLLECTION_NAME = "bookings";

    public static final String FIELD_CONCERT_ID = "concert_id";

    public static final String FIELD_CONCERT_OWNER_ID = "concert_owner_id";

    public static final String FIELD_STATUS = "status";

    @BsonProperty(value = FIELD_CONCERT_ID)
    String concertId;

    @BsonProperty(value = FIELD_CONCERT_OWNER_ID)
    String concertOwnerId;

    @BsonProperty(value = FIELD_STATUS)
    @Builder.Default
    BookingStatus status = BookingStatus.PENDING;

}

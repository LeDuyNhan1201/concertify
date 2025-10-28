package org.tma.intern.common.base;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseMongoModel {

    public static final String DB_CONCERT_SERVICE = "concert-service";

    public static final String DB_BOOKING_SERVICE = "booking-service";

    public static final String FIELD_ID = "_id";

    @BsonId
    ObjectId id;

    public String getIdHexString() {
        return this.id.toHexString();
    }

}

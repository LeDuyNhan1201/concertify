package org.tma.intern.concert.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.tma.intern.common.base.BaseMongoModel;
import org.tma.intern.common.type.SeatStatus;
import org.tma.intern.common.type.SeatType;

import java.time.Instant;

@MongoEntity(database = BaseMongoModel.DB_CONCERT_SERVICE, collection = Seat.COLLECTION_NAME)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Seat extends BaseMongoModel {

    public static final String COLLECTION_NAME = "seats";

    public static final String FIELD_CONCERT_ID = "concert_id";

    public static final String FIELD_CODE = "code";

    public static final String FIELD_TYPE = "type";

    public static final String FIELD_PRICE = "price";

    public static final String FIELD_STATUS = "status";

    public static final String FIELD_HELD_BY = "held_by";

    public static final String FIELD_HELD_AT = "held_at";

    @BsonProperty(FIELD_CONCERT_ID)
    String concertId;

    @BsonProperty(FIELD_CODE)
    String code;

    @BsonProperty(FIELD_TYPE)
    @Builder.Default
    SeatType type = SeatType.STANDARD;

    @BsonProperty(FIELD_PRICE)
    @Builder.Default
    double price = SeatType.STANDARD.price;

    @BsonProperty(FIELD_STATUS)
    @Builder.Default
    SeatStatus status = SeatStatus.AVAILABLE;

    @BsonProperty(FIELD_HELD_BY)
    String heldBy;

    @BsonProperty(FIELD_HELD_AT)
    Instant heldAt;

}

package org.tma.intern.booking.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.tma.intern.common.base.BaseMongoModel;

@MongoEntity(database = BaseMongoModel.DB_BOOKING_SERVICE, collection = BookingItem.COLLECTION_NAME)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingItem extends BaseMongoModel {

    public static final String COLLECTION_NAME = "booking_items";

    public static final String FIELD_BOOKING_ID = "booking_id";

    public static final String FIELD_SEAT_ID = "seat_id";

    public static final String FIELD_SEAT_CODE = "seat_code";

    public static final String FIELD_PRICE = "price";

    @BsonId
    private ObjectId id;

    @BsonProperty(FIELD_BOOKING_ID)
    private String bookingId;

    @BsonProperty(FIELD_SEAT_ID)
    private String seatId;

    @BsonProperty(FIELD_SEAT_CODE)
    private String seatCode;

    @BsonProperty(FIELD_PRICE)
    private double price;

}

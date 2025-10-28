package org.tma.intern.concert.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.tma.intern.common.base.AuditCollection;
import org.tma.intern.common.base.BaseMongoModel;
import org.tma.intern.common.type.Region;

import java.time.Instant;

@MongoEntity(database = BaseMongoModel.DB_CONCERT_SERVICE, collection = Concert.COLLECTION_NAME)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Concert extends AuditCollection {

    public static final String COLLECTION_NAME = "concerts";

    public static final String FIELD_TITLE = "title";

    public static final String FIELD_REGION = "region";

    public static final String FIELD_LOCATION = "location";

    public static final String FIELD_START_TIME = "start_time";

    public static final String FIELD_END_TIME = "end_time";

    public static final String FIELD_IS_APPROVED = "is_approved";

    @BsonProperty(FIELD_TITLE)
    String title;

    @BsonProperty(FIELD_REGION)
    Region region;

    @BsonProperty(FIELD_LOCATION)
    String location;

    @BsonProperty(FIELD_START_TIME)
    Instant startTime;

    @BsonProperty(FIELD_END_TIME)
    Instant endTime;

    @BsonProperty(FIELD_IS_APPROVED)
    @Builder.Default
    boolean isApproved = true;

}

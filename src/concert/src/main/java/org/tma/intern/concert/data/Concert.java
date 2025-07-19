package org.tma.intern.concert.data;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.tma.intern.common.base.AuditCollection;
import org.tma.intern.common.type.Region;

import java.time.Instant;

@MongoEntity(database = "concert-service", collection = "concerts")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Concert extends AuditCollection {

    @BsonProperty(value = "title")
    String title;

    @BsonProperty(value = "region")
    Region region;

    @BsonProperty(value = "location")
    String location;

    @BsonProperty(value = "start_time")
    Instant startTime;

    @BsonProperty(value = "end_time")
    Instant endTime;

    @BsonProperty(value = "is_approved")
    @Builder.Default
    boolean isApproved = true;

}

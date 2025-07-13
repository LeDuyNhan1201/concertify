package org.tma.intern.common.base;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class AuditCollection {

    @BsonId
    ObjectId id;

    @BsonProperty(value = "created_by")
    @Builder.Default
    String createdBy = "anonymous";

    @BsonProperty(value = "updated_by")
    @Builder.Default
    String updatedBy = "anonymous";

    @BsonProperty(value = "is_deleted")
    @Builder.Default
    boolean isDeleted = false;

    @BsonProperty(value = "created_at")
    @Builder.Default
    Instant createdAt = Instant.now();

    @BsonProperty(value = "updated_at")
    @Builder.Default
    Instant updatedAt = Instant.now();

    @BsonProperty(value = "version")
    @Builder.Default
    long version = 1;

}

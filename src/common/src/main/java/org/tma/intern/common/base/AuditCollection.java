package org.tma.intern.common.base;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public class AuditCollection extends BaseMongoModel {

    public static final String FIELD_CREATED_BY = "created_by";

    public static final String FIELD_UPDATED_BY = "updated_by";

    public static final String FIELD_IS_DELETED = "is_deleted";

    public static final String FIELD_CREATED_AT = "created_at";

    public static final String FIELD_UPDATED_AT = "updated_at";

    @BsonProperty(FIELD_CREATED_BY)
    @Builder.Default
    protected String createdBy = "anonymous";

    @BsonProperty(FIELD_UPDATED_BY)
    @Builder.Default
    protected String updatedBy = "anonymous";

    @BsonProperty(FIELD_IS_DELETED)
    @Builder.Default
    protected boolean isDeleted = false;

    @BsonProperty(FIELD_CREATED_AT)
    @Builder.Default
    protected Instant createdAt = Instant.now();

    @BsonProperty(FIELD_UPDATED_AT)
    @Builder.Default
    protected Instant updatedAt = Instant.now();

}

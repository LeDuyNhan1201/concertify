package org.tma.intern.common.base;

import org.bson.types.ObjectId;
import org.tma.intern.common.helper.StringHelper;
import org.tma.intern.common.helper.TimeHelper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public interface BaseMapper {

    default Instant map(LocalDateTime dateTime) {
        return TimeHelper.toInstant(dateTime);
    }

    default String map(Instant instant) {
        return TimeHelper.format(instant, TimeHelper.yyyyMMdd_HHmmss);
    }

    default String map(ObjectId objectId) {
        return objectId == null ? null : objectId.toHexString();
    }

    default ObjectId map(String strId) {
        return StringHelper.safeParse(strId);
    }

}

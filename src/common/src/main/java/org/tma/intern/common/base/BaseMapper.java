package org.tma.intern.common.base;

import org.bson.types.ObjectId;
import org.tma.intern.common.helper.TimeHelper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public interface BaseMapper {

    default Instant map(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneOffset.UTC).toInstant();
    }

    default String map(Instant instant) {
        return instant == null ? null : TimeHelper.format(instant, TimeHelper.yyyyMMdd_HHmmss);
    }

    default String map(ObjectId instant) {
        return instant == null ? null : instant.toHexString();
    }

}

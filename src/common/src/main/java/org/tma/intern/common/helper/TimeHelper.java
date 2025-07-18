package org.tma.intern.common.helper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeHelper {

    public static final String yyyyMMdd_HHmmss = "yyyy MM dd HH:mm:ss";

    public static String format(Instant time, String pattern) {
        if (time == null) return null;
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(time, zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern((pattern.isEmpty()) ? yyyyMMdd_HHmmss : pattern);
        return localDateTime.format(formatter);
    }

    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneOffset.UTC).toInstant();
    }

}

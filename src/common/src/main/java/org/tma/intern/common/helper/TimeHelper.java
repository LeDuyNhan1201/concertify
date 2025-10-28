package org.tma.intern.common.helper;

import java.time.*;
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

    public static String since(Instant startedAt) {
        if (startedAt == null) {
            return "-";
        }
        return toMsFormat(sinceMs(startedAt));
    }

    public static String toMsFormat(double duration) {
        return String.format("%.2f ms", duration);
    }

    public static double sinceMs(Instant startedAt) {
        if (startedAt == null) {
            return -1;
        }
        return sinceNs(startedAt) / 1_000_000.0;
    }

    public static double sinceNs(Instant startedAt) {
        if (startedAt == null) {
            return -1;
        }
        return Math.abs(Duration.between(startedAt, Instant.now()).toNanos());
    }

}

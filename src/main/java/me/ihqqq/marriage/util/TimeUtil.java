package me.ihqqq.marriage.util;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;


public final class TimeUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private TimeUtil() {
    }

    
    public static String formatDate(long millis) {
        return DATE_FORMAT.format(new Date(millis));
    }

    public static String formatDateTime(long millis, String pattern) {
        SimpleDateFormat formatter;
        try {
            formatter = pattern == null || pattern.isBlank()
                    ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    : new SimpleDateFormat(pattern);
        } catch (IllegalArgumentException ex) {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        return formatter.format(new Date(millis));
    }

    
    public static long daysBetween(long since) {
        long now = System.currentTimeMillis();
        long diff = Math.max(0L, now - since);
        return diff / (24L * 60L * 60L * 1000L);
    }
}

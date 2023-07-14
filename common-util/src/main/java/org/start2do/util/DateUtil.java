package org.start2do.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtil {

    private ConcurrentHashMap<String, DateTimeFormatter> map;
    @Setter
    @Getter
    private Locale locale = Locale.CHINA;

    static {
        map = new ConcurrentHashMap();
        putPattern(Pattern.YYYY_MM_ddHHmmss);
        putPattern(Pattern.YYYY_MM_dd);
        putPattern(Pattern.HHmmss);
    }

    private void putPattern(String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            return;
        }
        map.put(pattern, DateTimeFormatter.ofPattern(pattern, locale));
    }

    private DateTimeFormatter getPattern(String pattern) {
        if (StringUtils.isEmpty(pattern)) {
            throw new RuntimeException("Pattern 不能为空");
        }
        DateTimeFormatter formatter = map.get(pattern);

        if (formatter == null) {
            formatter = DateTimeFormatter.ofPattern(pattern);
            map.put(pattern, formatter);
        }
        return formatter;
    }

    public String LocalDateTimeToString(LocalDateTime time, String pattern) {
        if (time == null) {
            return "";
        }
        return time.format(getPattern(pattern));
    }

    public String LocalDateToString(LocalDate time, String pattern) {
        if (time == null) {
            return "";
        }
        return time.format(getPattern(pattern));
    }

    public String LocalTimeToString(LocalTime time, String pattern) {
        if (time == null) {
            return "";
        }
        return time.format(getPattern(pattern));
    }

    public LocalTime StringToLocalTime(String time, String pattern) {
        if (time == null) {
            return LocalTime.now();
        }
        return LocalTime.parse(time, getPattern(pattern));
    }

    public LocalDate StringToLocalDate(String time, String pattern) {
        if (time == null) {
            return LocalDate.now();
        }
        return LocalDate.parse(time, getPattern(pattern));
    }

    public LocalDateTime StringToLocalDateTime(String time, String pattern) {
        if (time == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.parse(time, getPattern(pattern));
    }

    public static LocalDateTime dateToLocalDateTime(Date endDate) {
        return LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());
    }

    public static String dateToString(Date date, String yyyyMmDdHHmmss) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(yyyyMmDdHHmmss).format(date);
    }

    public static String LocalDateTimeStr() {
        return LocalDateTimeToString(LocalDateTime.now(), Pattern.YYYY_MM_ddHHmmss);
    }

    public static LocalTime toLocalTime(String string, String hHmmss) {
        return LocalTime.parse(string, getPattern(hHmmss));
    }

    public static LocalDate toLocalDate(String reportTime, String yyyyMmDd) {
        return LocalDate.parse(reportTime, getPattern(yyyyMmDd));
    }

    public static long toTimestamp(String time, String pattern, ZoneOffset timeZone) {
        return LocalDateTime.parse(time, getPattern(pattern)).toInstant(timeZone).toEpochMilli();
    }

    public static long toTimestamp(LocalDateTime time) {
        return time.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    public static LocalDateTime timestampToLocalDateTime(Long timestamp, ZoneOffset offset) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), offset);
    }

    public static class Pattern {

        public static final String YYYY_MM_ddHHmmss = "yyyy-MM-dd HH:mm:ss";
        public static final String YYYY_MM_dd = "yyyy-MM-dd";
        public static final String HHmmss = "HH:mm:ss";
        public static final String HHmm = "HH:mm";
    }


}

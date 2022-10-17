package org.start2do;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeUtil {

    private final DateTimeFormatter YYYYMMddHHmmss = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter LocalTimeDefault = DateTimeFormatter.ofPattern("HH:mm:ss");

    public String LocalTimeToString(LocalTime time, DateTimeFormatter formatter) {
        if (time == null) {
            return null;
        }
        if (formatter == null) {
            formatter = LocalTimeDefault;
        }
        return time.format(formatter);
    }

    public String LocalDateToString(LocalDateTime time, DateTimeFormatter formatter) {
        if (time == null) {
            return null;
        }
        if (formatter == null) {
            formatter = YYYYMMddHHmmss;
        }
        return time.format(formatter);
    }

    public static LocalDateTime StringToLocalDateTime(String time, DateTimeFormatter formatter) {
        if (formatter == null) {
            return LocalDateTime.parse(time, YYYYMMddHHmmss);
        }
        return LocalDateTime.parse(time, formatter);
    }
}

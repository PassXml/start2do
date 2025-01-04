package test;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.start2do.util.DateUtil;
import org.start2do.util.DateUtil.TimeRange;

public class DateUtilsTest {

    @Test
    void test1() {
        LocalDateTime now = LocalDateTime.of(1990, 10, 10, 10, 10, 0);
        int minutes = 10;
        TimeRange timRange = DateUtil.getTimRange(now, minutes);
        System.out.println(timRange);
        Assertions.assertEquals(now, timRange.getEndTime());
        Assertions.assertEquals(now.minusMinutes(minutes), timRange.getStartTime());
    }
}

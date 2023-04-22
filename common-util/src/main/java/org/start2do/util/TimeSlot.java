package org.start2do.util;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * 时间段
 */
public class TimeSlot {

    private LocalTime startTime;
    private LocalTime endTime;

    public TimeSlot(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean overlaps(TimeSlot other) {
        return (this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime));
    }

    public long duration() {
        return ChronoUnit.MINUTES.between(startTime, endTime);
    }

    public static void main(String[] args) {
        TimeSlot slot1 = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(10, 0));
        TimeSlot slot2 = new TimeSlot(LocalTime.of(9, 30), LocalTime.of(11, 0));
        TimeSlot slot3 = new TimeSlot(LocalTime.of(11, 0), LocalTime.of(12, 0));

        System.out.println("Slot 1 overlaps with slot 2: " + slot1.overlaps(slot2));
        System.out.println("Slot 1 overlaps with slot 3: " + slot1.overlaps(slot3));
        System.out.println("Slot 1 duration: " + slot1.duration() + " minutes");
    }
}

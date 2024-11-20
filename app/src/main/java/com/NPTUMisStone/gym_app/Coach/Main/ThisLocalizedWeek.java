package com.NPTUMisStone.gym_app.Coach.Main;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class ThisLocalizedWeek {

    private final static ZoneId TZ = ZoneId.systemDefault(); // 使用系統的時區

    private final Locale locale;
    private final DayOfWeek firstDayOfWeek;
    private final DayOfWeek lastDayOfWeek;

    public ThisLocalizedWeek(final Locale locale) {
        this.locale = locale;
        this.firstDayOfWeek = DayOfWeek.MONDAY; // 台灣習慣週一起始
        this.lastDayOfWeek = DayOfWeek.SUNDAY; // 週日結束
    }

    public LocalDate getFirstDay() {
        return LocalDate.now(TZ).with(TemporalAdjusters.previousOrSame(this.firstDayOfWeek));
    }

    public LocalDate getLastDay() {
        return LocalDate.now(TZ).with(TemporalAdjusters.nextOrSame(this.lastDayOfWeek));
    }

    public DayOfWeek getFirstDayOfWeek() {
        return this.firstDayOfWeek;
    }

    public DayOfWeek getLastDayOfWeek() {
        return this.lastDayOfWeek;
    }

    @Override
    public String toString() {
        return String.format("The %s week starts on %s and ends on %s",
                this.locale.getDisplayName(),
                this.firstDayOfWeek,
                this.lastDayOfWeek);
    }
}

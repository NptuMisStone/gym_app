package com.NPTUMisStone.gym_app.Coach.Main;

import androidx.annotation.NonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class ThisLocalizedWeek {

    private final static ZoneId TZ = ZoneId.systemDefault(); // 使用系統的時區

    private final Locale locale;
    private final DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY; // 設定週一為起始日
    private final DayOfWeek lastDayOfWeek;

    public ThisLocalizedWeek(final Locale locale) {
        this.locale = locale;
        this.lastDayOfWeek = DayOfWeek.SUNDAY; // 設定週日為結束日
    }

    public LocalDate getFirstDay() {
        return LocalDate.now(TZ).with(TemporalAdjusters.previousOrSame(this.firstDayOfWeek));
    }

    public LocalDate getLastDay() {
        return LocalDate.now(TZ).with(TemporalAdjusters.nextOrSame(this.lastDayOfWeek));
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("The %s week starts on %s and ends on %s",
                this.locale.getDisplayName(),
                this.firstDayOfWeek,
                this.lastDayOfWeek);
    }
}

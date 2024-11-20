package com.NPTUMisStone.gym_app.Coach.Scheduled;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.NPTUMisStone.gym_app.Coach.Main.ThisLocalizedWeek;
import com.NPTUMisStone.gym_app.R;
import com.harrywhewell.scrolldatepicker.DayScrollDatePicker;
import com.harrywhewell.scrolldatepicker.OnDateSelectedListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DayScrollDatePicker mPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPicker = findViewById(R.id.day_date_picker);

        // 初始化當前周的起始和結束日期
        updateWeekRange(LocalDate.now());

        // 設置日期選擇事件
        mPicker.getSelectedDate(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@Nullable Date date) {
                if (date != null) {
                    // 轉換 Date 為 LocalDate
                    LocalDate selectedDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    // 更新 UI 和日期範圍
                    Toast.makeText(MainActivity.this, "選擇的日期: " + selectedDate, Toast.LENGTH_SHORT).show();
                    updateWeekRange(selectedDate);
                }
            }
        });
    }

    /**
     * 更新週的起始和結束日期
     * @param date 當前選擇的日期
     */
    private void updateWeekRange(LocalDate date) {
        ThisLocalizedWeek week = new ThisLocalizedWeek(Locale.TAIWAN);

        // 計算起始和結束日期
        LocalDate firstDay = date.with(java.time.temporal.TemporalAdjusters.previousOrSame(week.getFirstDayOfWeek()));
        LocalDate lastDay = date.with(java.time.temporal.TemporalAdjusters.nextOrSame(week.getLastDayOfWeek()));

        // 設定日期範圍
        mPicker.setStartDate(firstDay.getDayOfMonth(), firstDay.getMonthValue(), firstDay.getYear());
        mPicker.setEndDate(lastDay.getDayOfMonth(), lastDay.getMonthValue(), lastDay.getYear());

        Log.d("MainActivity", "週起始日: " + firstDay + ", 結束日: " + lastDay);
    }
}

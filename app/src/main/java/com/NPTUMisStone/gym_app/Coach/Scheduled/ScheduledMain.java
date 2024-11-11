package com.NPTUMisStone.gym_app.Coach.Scheduled;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduledMain extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private TextView dateDisplay;
    private Calendar currentWeekCalendar;

    // 自定義的 Course 類，用於存儲課程信息
    public class Course {
        private String courseName;
        private String startTime;
        private String endTime;
        private int locationType;
        private int scheduleId;

        public Course(String courseName, String startTime, String endTime, int locationType, int scheduleId) {
            this.courseName = courseName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.locationType = locationType;
            this.scheduleId = scheduleId;
        }

        // Getter 方法
        public String getCourseName() {
            return courseName;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public int getLocationType() {
            return locationType;
        }

        public int getScheduleId() {
            return scheduleId;
        }
    }

    Connection MyConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_scheduled_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        findViewById(R.id.ScheduledCheck_backButton).setOnClickListener(v -> finish());
        // 新增課表按鈕，點擊跳轉到 ScheduledAdd 頁面
        findViewById(R.id.ScheduledAdd_button).setOnClickListener(v -> {
            Intent intent = new Intent(ScheduledMain.this, ScheduledAdd.class);
            startActivity(intent);
        });

        // 初始化視圖和日期顯示
        dateDisplay = findViewById(R.id.ScheduledMain_dateDisplay);
        Button todayButton = findViewById(R.id.ScheduledMain_todayButton);
        recyclerView = findViewById(R.id.ScheduledCheck_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // 初始化適配器
        adapter = new CustomAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 設定當前周的日曆
        currentWeekCalendar = Calendar.getInstance();
        currentWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY); // 將星期一設為一週的第一天

        // 設置進入頁面時顯示今天的日期
        dateDisplay.setText(formatDate(currentWeekCalendar)); // 更新顯示的日期

        updateWeekCalendar(currentWeekCalendar);

        // 點擊 "回到今天" 按鈕
        todayButton.setOnClickListener(v -> {
            currentWeekCalendar = Calendar.getInstance(); // 設定為今天
            currentWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY); // 確保今天也按照星期一為第一天

            // 更新周曆和日期顯示
            updateWeekCalendar(currentWeekCalendar);
            dateDisplay.setText(formatDate(currentWeekCalendar)); // 更新顯示的日期

            loadScheduleForDay(currentWeekCalendar); // 加載今天的課程
        });

        // 設定每個日期按鈕的點擊事件
        for (int i = 1; i <= 7; i++) {
            int buttonId = getResources().getIdentifier("day" + i, "id", getPackageName());
            Button dayButton = findViewById(buttonId);
            int finalI = i;
            dayButton.setOnClickListener(v -> {
                Calendar selectedDay = (Calendar) currentWeekCalendar.clone();
                selectedDay.setFirstDayOfWeek(Calendar.MONDAY); // 確保選擇日期也以星期一為起點
                selectedDay.set(Calendar.DAY_OF_WEEK, finalI + Calendar.MONDAY - 1); // 調整索引
                loadScheduleForDay(selectedDay);
                dateDisplay.setText(formatDate(selectedDay)); // 更新顯示的日期
            });
        }

        // 初始加載今天的課程
        loadScheduleForDay(currentWeekCalendar);
    }

    private void updateWeekCalendar(Calendar calendar) {
        // 迭代一週的天數（從1到7），並設定按鈕
        for (int i = 1; i <= 7; i++) {
            int buttonId = getResources().getIdentifier("day" + i, "id", getPackageName());
            Button dayButton = findViewById(buttonId);

            // 克隆當前的日曆實例，然後設定當前的星期幾（從星期一到星期日）
            Calendar dayOfWeek = (Calendar) calendar.clone();
            dayOfWeek.setFirstDayOfWeek(Calendar.MONDAY);

            // 正確地設定星期幾，i=1 對應星期一，i=7 對應星期日
            dayOfWeek.set(Calendar.DAY_OF_WEEK, i + Calendar.MONDAY - 1);

            // 設定按鈕的文字為當天的日期
            dayButton.setText(String.valueOf(dayOfWeek.get(Calendar.DAY_OF_MONTH)));
        }
    }

    private void loadScheduleForDay(Calendar day) {
        // 從資料庫中獲取課程資料
        List<Course> courses = fetchCoursesForDay(day);

        // 清除並更新 RecyclerView 的數據
        adapter.courseList.clear();  // 使用 courseList，而不是 nameList
        adapter.courseList.addAll(courses);
        adapter.notifyDataSetChanged(); // 通知適配器數據已更新
    }

    private List<Course> fetchCoursesForDay(Calendar day) {
        List<Course> courses = new ArrayList<>();

        // 計算該周的開始和結束日期
        Calendar startOfWeek = (Calendar) day.clone();
        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Calendar endOfWeek = (Calendar) startOfWeek.clone();
        endOfWeek.add(Calendar.DAY_OF_WEEK, 6);  // 一周的最後一天（星期日）

        String searchQuery = "SELECT [課程名稱], [開始時間], [結束時間], [地點類型], [課表編號] " +
                "FROM [健身教練課表課程合併] " +
                "WHERE [日期] = ? " +  // 根據具體日期查詢
                "AND [健身教練編號] = ? " +
                "ORDER BY [開始時間]";

        try (PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery)) {
            // 設定查詢參數
            searchStatement.setString(1, formatDateForSQL(day));  // 設定具體日期
            searchStatement.setInt(2, Coach.getInstance().getCoachId());  // 設定教練ID

            // 執行查詢
            ResultSet resultSet = searchStatement.executeQuery();
            while (resultSet.next()) {
                // 獲取查詢結果中的數據
                String courseName = resultSet.getString("課程名稱");
                String startTime = resultSet.getString("開始時間");
                String endTime = resultSet.getString("結束時間");
                int locationType = resultSet.getInt("地點類型");
                int scheduleId = resultSet.getInt("課表編號");

                // 添加到課程列表
                courses.add(new Course(courseName, startTime, endTime, locationType, scheduleId));
            }
        } catch (SQLException e) {
            Log.e("ScheduledMain", "Error loading courses from DB", e);
        }

        return courses;
    }


    private String formatDateForSQL(Calendar calendar) {
        return calendar.get(Calendar.YEAR) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    private String formatDate(Calendar calendar) {
        // 格式化日期為 YYYY-MM-DD 格式
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    // 自定義適配器類，用於顯示課程列表
    static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        List<Course> courseList;

        CustomAdapter(List<Course> courses) {
            courseList = courses;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.coach_scheduled_main_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Course course = courseList.get(position);  // 獲取 Course 對象
            holder.textView.setText(course.getCourseName());  // 設置課程名稱
            holder.timeTextView.setText(course.getStartTime() + " - " + course.getEndTime());  // 設置時間範圍

            // 根據 locationType 設置不同的背景顏色
            if (course.getLocationType() == 2) {
                holder.courseCardLayout.setBackgroundResource(R.drawable.course_card_blue);
                holder.courseTime.setBackgroundResource(R.drawable.course_time_blue);
            } else {
                holder.courseCardLayout.setBackgroundResource(R.drawable.course_card_red);
                holder.courseTime.setBackgroundResource(R.drawable.course_time_red);
            }
        }

        @Override
        public int getItemCount() {
            return courseList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            TextView timeTextView;
            View courseCardLayout;
            TextView courseTime;

            ViewHolder(View view) {
                super(view);
                textView = view.findViewById(R.id.CheckItem_checkedText);
                timeTextView = view.findViewById(R.id.courseTime);
                courseCardLayout = view.findViewById(R.id.courseCardLayout);
                courseTime = view.findViewById(R.id.courseTime);
            }
        }
    }
}

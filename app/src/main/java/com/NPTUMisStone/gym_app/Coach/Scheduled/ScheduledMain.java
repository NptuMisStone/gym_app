package com.NPTUMisStone.gym_app.Coach.Scheduled;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        adapter = new CustomAdapter(this, new ArrayList<>());
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
        ScheduledMain scheduledMain; // 引用 ScheduledMain

        CustomAdapter(ScheduledMain scheduledMain, List<Course> courses) {
            this.scheduledMain = scheduledMain;
            this.courseList = courses;
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
            Course course = courseList.get(position); // 獲取 Course 對象
            holder.textView.setText(course.getCourseName()); // 設置課程名稱
            holder.timeTextView.setText(course.getStartTime() + " - " + course.getEndTime()); // 設置時間範圍

            // 根據 locationType 設置不同的背景顏色
            if (course.getLocationType() == 2) {
                holder.courseCardLayout.setBackgroundResource(R.drawable.course_card_blue);
                holder.courseTime.setBackgroundResource(R.drawable.course_time_blue);
            } else {
                holder.courseCardLayout.setBackgroundResource(R.drawable.course_card_red);
                holder.courseTime.setBackgroundResource(R.drawable.course_time_red);
            }

            // 點擊事件，轉換 int 為 String
            holder.itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                if (context instanceof ScheduledMain) {
                    ((ScheduledMain) context).showScheduleDetailsDialog(String.valueOf(course.getScheduleId()));
                }
            });
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

    private Map<String, String> getScheduleDetails(String scheduleId) {
        Map<String, String> scheduleDetails = new HashMap<>();
        String query = "SELECT [課程名稱], [日期], [課程時間長度], [開始時間], [結束時間] " +
                "FROM [健身教練課表課程合併] " +
                "WHERE [課表編號] = ?";

        try (PreparedStatement stmt = MyConnection.prepareStatement(query)) {
            stmt.setString(1, scheduleId);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                scheduleDetails.put("courseName", resultSet.getString("課程名稱"));
                scheduleDetails.put("date", resultSet.getDate("日期").toString()); // SQL 日期轉字符串
                scheduleDetails.put("duration", resultSet.getString("課程時間長度"));
                scheduleDetails.put("startTime", resultSet.getString("開始時間"));
                scheduleDetails.put("endTime", resultSet.getString("結束時間"));
            } else {
                scheduleDetails.put("error", "找不到相關課程信息。");
            }
        } catch (SQLException e) {
            Log.e("DatabaseError", "Error fetching schedule details", e);
            scheduleDetails.put("error", "資料庫錯誤，無法取得資訊。");
        }

        return scheduleDetails;
    }

    private void showScheduleDetailsDialog(String scheduleId) {
        // 獲取課程詳細資訊
        Map<String, String> details = getScheduleDetails(scheduleId);

        // 構建顯示訊息
        StringBuilder messageBuilder = new StringBuilder();
        if (details.containsKey("error")) {
            messageBuilder.append(details.get("error"));
        } else {
            messageBuilder.append("課程名稱: ").append(details.get("courseName")).append("\n")
                    .append("日期: ").append(details.get("date")).append("\n")
                    .append("課程時間長度: ").append(details.get("duration")).append("\n")
                    .append("開始時間: ").append(details.get("startTime")).append("\n")
                    .append("結束時間: ").append(details.get("endTime"));
        }

        // 創建 AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("課程詳情")
                .setMessage(messageBuilder.toString())
                .setCancelable(false) // 禁止點擊外部取消對話框
                .create();

        // 自定義按鈕
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "刪除課表", (dialogInterface, which) -> {
            // 確認刪除
            confirmDeleteSchedule(scheduleId);
        });
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "關閉", (dialogInterface, which) -> dialogInterface.dismiss());

        // 顯示對話框後設置按鈕樣式
        dialog.show();

        // 將刪除按鈕設置為紅色
        Button deleteButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (deleteButton != null) {
            deleteButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // 將關閉按鈕設置為默認樣式
        Button closeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (closeButton != null) {
            closeButton.setTextColor(getResources().getColor(android.R.color.black));
        }
    }
    private void confirmDeleteSchedule(String scheduleId) {
        // 創建一個刪除確認對話框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("刪除班表")
                .setMessage("您確定要刪除此班表嗎？此操作無法撤銷。")
                .setPositiveButton("刪除", (dialog, which) -> {
                    // 執行刪除操作
                    deleteSchedule(scheduleId);
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        // 顯示對話框
        AlertDialog dialog = builder.create();
        dialog.show();

        // 將刪除按鈕設置為紅色
        Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (deleteButton != null) {
            deleteButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
    private void deleteSchedule(String scheduleId) {
        if (scheduleId == null || scheduleId.isEmpty()) {
            // 如果課表編號無效，顯示錯誤訊息
            showToast("❌ 刪除失敗：課表編號無效");
            return;
        }

        String deleteQuery = "DELETE FROM 健身教練課表 WHERE 課表編號 = ?";
        try (PreparedStatement stmt = MyConnection.prepareStatement(deleteQuery)) {
            // 設置刪除參數
            stmt.setString(1, scheduleId);

            // 執行刪除操作
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // 刪除成功
                showToast("✅ 刪除成功：班表已刪除");

                // 刷新課程列表
                loadScheduleForDay(currentWeekCalendar);
            } else {
                // 刪除失敗，找不到該課表編號
                showToast("❌ 刪除失敗：找不到對應的課表編號");
            }
        } catch (SQLException e) {
            Log.e("DatabaseError", "Error deleting schedule", e);
            showToast("❌ 刪除失敗：無法刪除課表，請稍後再試");
        }
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}

package com.NPTUMisStone.gym_app.Coach.Scheduled;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Locale;
import java.util.Map;
import android.widget.LinearLayout;


public class ScheduledMain extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private TextView dateDisplay;
    private Calendar currentWeekCalendar;
    private Button selectedButton; // 用於記錄當前選中的按鈕
    private long lastClickTime = 0;
    private static final long CLICK_DELAY = 500; // 500毫秒

    private boolean isClickable() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_DELAY) {
            return false; // 忽略過於快速的點擊
        }
        lastClickTime = currentTime;
        return true;
    }



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

        // 初始化數據庫連接
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();

        // 初始化系統視圖
        setupWindowInsets();
        setupNavigationButtons();

        // 初始化 RecyclerView 和日期顯示
        initializeViews();

        // 設定當前周的日曆
        currentWeekCalendar = Calendar.getInstance();
        currentWeekCalendar.setFirstDayOfWeek(Calendar.MONDAY);

        // 初始化上次選中的日期為今天
        lastSelectedDay = (Calendar) currentWeekCalendar.clone();
        resetTime(lastSelectedDay);

        // **在這裡設置當日日期到 ScheduledMain_dateDisplay**
        dateDisplay.setText(formatDate(lastSelectedDay));

        // 更新日期顯示和按鈕狀態
        updateWeekCalendar(currentWeekCalendar);
        highlightTodayButton(); // 設置紅框

        // 設置選中按鈕（橘底）
        updateButtonSelection(getButtonIndexForDay(lastSelectedDay));

        // 加載今天的課程
        loadScheduleForDay(currentWeekCalendar);

        // 點擊「回到今天」
        findViewById(R.id.ScheduledMain_todayButton).setOnClickListener(v -> {
            View weekContainer = findViewById(R.id.ScheduledMain_weekContainer);

            // 獲取今天的日期
            Calendar today = Calendar.getInstance();
            today.setFirstDayOfWeek(Calendar.MONDAY);
            resetTime(today);

            // 檢查當前週範圍
            Calendar startOfWeek = (Calendar) currentWeekCalendar.clone();
            startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            resetTime(startOfWeek);

            Calendar endOfWeek = (Calendar) startOfWeek.clone();
            endOfWeek.add(Calendar.DAY_OF_WEEK, 6);

            if (!today.before(startOfWeek) && !today.after(endOfWeek)) {
                // 如果今天在本週內，直接更新顯示，跳過動畫
                highlightTodayButton();
                updateButtonSelection(getButtonIndexForDay(today));
                updateDisplayedDateAndSchedule(today);
                return;
            }

            // 計算目標週與當前週的差值
            int weekDifference = today.get(Calendar.WEEK_OF_YEAR) - currentWeekCalendar.get(Calendar.WEEK_OF_YEAR);

            // 設定動畫方向
            int translationX = weekDifference > 0 ? weekContainer.getWidth() : -weekContainer.getWidth();

            // 平移動畫
            ObjectAnimator exitAnimator = ObjectAnimator.ofFloat(weekContainer, "translationX", 0, -translationX);
            ObjectAnimator enterAnimator = ObjectAnimator.ofFloat(weekContainer, "translationX", translationX, 0);

            exitAnimator.setDuration(300);
            enterAnimator.setDuration(300);

            // 在退出動畫結束後更新到今天的邏輯
            exitAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // 更新 currentWeekCalendar 為今天的週
                    currentWeekCalendar = (Calendar) today.clone();

                    // 更新週顯示
                    updateWeekCalendar(currentWeekCalendar);

                    // 高亮今天按鈕
                    highlightTodayButton();

                    // 更新選中按鈕（橘底）
                    updateButtonSelection(getButtonIndexForDay(today));

                    // 更新日期顯示和課程列表
                    updateDisplayedDateAndSchedule(today);

                    // 播放進入動畫
                    enterAnimator.start();
                }
            });

            // 播放退出動畫
            exitAnimator.start();
        });




        // 設定每個日期按鈕的點擊事件
        setupDayButtonClickListeners();

        // 初始化手勢偵測器
        GestureDetector gestureDetector = new GestureDetector(this, new GestureListener());

        findViewById(R.id.weekContainerWrapper).setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true; // 確保事件不被子視圖攔截
        });

        // 為每個按鈕添加觸摸事件，並傳遞給手勢偵測器
        for (int i = 1; i <= 7; i++) {
            int buttonId = getResources().getIdentifier("day" + i, "id", getPackageName());
            Button dayButton = findViewById(buttonId);

            dayButton.setOnTouchListener((v, event) -> {
                // 傳遞手勢事件
                gestureDetector.onTouchEvent(event);

                // 同時保留按鈕的點擊功能
                return false;
            });
        }

        findViewById(R.id.ScheduledMain_findButton).setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();

            // 設置默認語系，確保日曆從周一開始
            Locale.setDefault(Locale.TAIWAN);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ScheduledMain.this,
                    (view, year, month, dayOfMonth) -> {
                        // 處理選中日期
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        resetTime(selectedDate);

                        currentWeekCalendar = (Calendar) selectedDate.clone();
                        currentWeekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

                        updateDisplayedDateAndSchedule(selectedDate);
                        updateWeekCalendar(currentWeekCalendar);
                        updateButtonSelection(getButtonIndexForDay(selectedDate));
                        highlightTodayButton();
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
            );

            // 確保 CalendarView 的第一天為周一
            DatePicker datePicker = datePickerDialog.getDatePicker();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    datePicker.setFirstDayOfWeek(Calendar.MONDAY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 顯示日期選擇對話框
            datePickerDialog.show();
        });

    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupNavigationButtons() {
        findViewById(R.id.ScheduledCheck_backButton).setOnClickListener(v -> finish());
        findViewById(R.id.ScheduledAdd_button).setOnClickListener(v -> {
            Intent intent = new Intent(ScheduledMain.this, ScheduledAdd.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        dateDisplay = findViewById(R.id.ScheduledMain_dateDisplay);
        recyclerView = findViewById(R.id.ScheduledCheck_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new CustomAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }
    private void updateWeekCalendar(Calendar calendar) {
        for (int i = 1; i <= 7; i++) {
            int buttonId = getResources().getIdentifier("day" + i, "id", getPackageName());
            Button dayButton = findViewById(buttonId);

            if (dayButton != null) {
                // 獲取本週的第i天（周一開始）
                Calendar dayOfWeek = (Calendar) calendar.clone();
                dayOfWeek.setFirstDayOfWeek(Calendar.MONDAY);
                dayOfWeek.set(Calendar.DAY_OF_WEEK, i + Calendar.MONDAY - 1);

                // 更新按鈕文字為該天日期
                dayButton.setText(String.valueOf(dayOfWeek.get(Calendar.DAY_OF_MONTH)));
            }
        }
    }
    private void highlightTodayButton() {
        Calendar today = Calendar.getInstance();
        resetTime(today);

        // 確認今天是否在當前週
        Calendar startOfWeek = (Calendar) currentWeekCalendar.clone();
        resetTime(startOfWeek);

        Calendar endOfWeek = (Calendar) startOfWeek.clone();
        endOfWeek.add(Calendar.DAY_OF_WEEK, 6);

        for (int i = 1; i <= 7; i++) {
            int buttonId = getResources().getIdentifier("day" + i, "id", getPackageName());
            Button button = findViewById(buttonId);

            if (button != null) {
                if (!today.before(startOfWeek) && !today.after(endOfWeek)) {
                    // 今天在當前週，設置紅框
                    if (i == getButtonIndexForDay(today)) {
                        button.setActivated(true); // 紅框
                    } else {
                        button.setActivated(false); // 清除其他紅框
                    }
                } else {
                    // 今天不在當前週，清除所有紅框
                    button.setActivated(false);
                }
            }
        }
    }


    private void updateButtonSelection(int dayIndex) {
        // 清除之前選中的按鈕樣式
        if (selectedButton != null) {
            selectedButton.setSelected(false);
            selectedButton.setTextColor(getResources().getColor(R.color.default_text_color)); // 默認文字顏色
        }

        // 設置當前選中按鈕樣式
        int buttonId = getResources().getIdentifier("day" + dayIndex, "id", getPackageName());
        Button dayButton = findViewById(buttonId);

        if (dayButton != null) {
            dayButton.setSelected(true); // 橘底
            dayButton.setTextColor(getResources().getColor(R.color.selected_text_color)); // 白色文字
            selectedButton = dayButton;
        }

        // 更新紅框（只高亮今天）
        highlightTodayButton();
    }


    private void setupDayButtonClickListeners() {
        for (int i = 1; i <= 7; i++) {
            int buttonId = getResources().getIdentifier("day" + i, "id", getPackageName());
            Button dayButton = findViewById(buttonId);

            final int dayIndex = i;
            dayButton.setOnClickListener(v -> {
                if (!isClickable()) {
                    return; // 忽略快速點擊
                }

                // 更新選中按鈕（橘底）
                updateButtonSelection(dayIndex);

                // 更新顯示日期和課程
                Calendar selectedDay = (Calendar) currentWeekCalendar.clone();
                selectedDay.setFirstDayOfWeek(Calendar.MONDAY);
                selectedDay.set(Calendar.DAY_OF_WEEK, dayIndex + Calendar.MONDAY - 1);
                updateDisplayedDateAndSchedule(selectedDay);
            });
        }
    }

    private Calendar lastSelectedDay = null; // 保存上一個選中的日期
    private void updateDisplayedDateAndSchedule(Calendar selectedDay) {
        dateDisplay.setText(formatDate(selectedDay));

        resetTime(selectedDay);

        // 比較與之前選中的日期，確保不重複動畫
        if (lastSelectedDay != null && selectedDay.equals(lastSelectedDay)) {
            return; // 選中相同日期時不執行更新
        }

        // 更新課程列表動畫
        if (lastSelectedDay != null && selectedDay.before(lastSelectedDay)) {
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_slide_in_left));
        } else if (lastSelectedDay != null) {
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_slide_in_right));
        }

        recyclerView.scheduleLayoutAnimation();

        // 更新上一個選中的日期
        lastSelectedDay = (Calendar) selectedDay.clone();

        // 加載新日期的課程
        loadScheduleForDay(selectedDay);
    }

    private void resetTime(Calendar calendar) {
        calendar.setFirstDayOfWeek(Calendar.MONDAY); // 設定周一為第一天
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
    private String formatDate(Calendar calendar) {
        return calendar.get(Calendar.YEAR) + "-" +
                (calendar.get(Calendar.MONTH) + 1) + "-" +
                calendar.get(Calendar.DAY_OF_MONTH);
    }
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100; // 滑動距離閾值
        private static final int SWIPE_VELOCITY_THRESHOLD = 100; // 滑動速度閾值

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();

            // 垂直滑動忽略
            if (Math.abs(diffX) > Math.abs(e2.getY() - e1.getY())) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // 向右滑動 -> 前一週
                        animateWeekTransition(false);
                    } else {
                        // 向左滑動 -> 下一週
                        animateWeekTransition(true);
                    }
                    return true;
                }
            }
            return false;
        }
    }
    private void animateWeekTransition(boolean isNextWeek) {
        View weekContainer = findViewById(R.id.ScheduledMain_weekContainer);
        int translationX = isNextWeek ? weekContainer.getWidth() : -weekContainer.getWidth();

        // 平移進出動畫
        ObjectAnimator exitAnimator = ObjectAnimator.ofFloat(weekContainer, "translationX", 0, -translationX);
        ObjectAnimator enterAnimator = ObjectAnimator.ofFloat(weekContainer, "translationX", translationX, 0);

        exitAnimator.setDuration(300);
        enterAnimator.setDuration(300);

        exitAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isNextWeek) {
                    currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, 1);
                } else {
                    currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, -1);
                }

                // 更新對應的選中日期
                if (lastSelectedDay != null) {
                    lastSelectedDay.add(Calendar.WEEK_OF_YEAR, isNextWeek ? 1 : -1);
                } else {
                    lastSelectedDay = (Calendar) currentWeekCalendar.clone();
                    lastSelectedDay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                }
                resetTime(lastSelectedDay);

                // 更新週顯示
                updateWeekCalendar(currentWeekCalendar);

                // 重新檢查並高亮今天
                highlightTodayButton();

                // 更新選中按鈕
                updateButtonSelection(getButtonIndexForDay(lastSelectedDay));

                // 更新日期顯示和課程列表
                updateDisplayedDateAndSchedule(lastSelectedDay);

                enterAnimator.start();
            }
        });
        exitAnimator.start();
    }
    private int getButtonIndexForDay(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 原始值：週日為1
        return (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - Calendar.MONDAY + 1; // 調整為周一開始
    }


    //-----------------------------------------------------------------------------

    private void loadScheduleForDay(Calendar day) {
        // 獲取課程數據
        List<Course> courses = fetchCoursesForDay(day);

        // 更新 RecyclerView 的數據
        adapter.courseList.clear();
        adapter.courseList.addAll(courses);
        adapter.notifyDataSetChanged();

        // 切換 RecyclerView 和 EmptyView 的可見性
        TextView emptyView = findViewById(R.id.empty_view);
        if (courses.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
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

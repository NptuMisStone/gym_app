package com.NPTUMisStone.gym_app.Coach.Scheduled;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.hdev.calendar.bean.DateInfo;
import com.hdev.calendar.view.MultiCalendarView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class ScheduledAdd extends AppCompatActivity {

    List<DateInfo> dateInfoList = new ArrayList<>();
    boolean isDialogShow = false;
    Connection MyConnection;
    int classPeriod = 0; // 儲存當前課程時長
    int selectedClassId = -1; // 保存選中的 classId
    private int selectedCourseIndex = 0; // 默認為第一個課程


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coach_scheduled_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();

        // 返回按鈕
        findViewById(R.id.ScheduledAdd_backButton).setOnClickListener(v -> finish());

        // 初始化按鈕和輸入框
        initDefaultTime();
        initDateSelectionButton();
        initCourseSelectionButton();

        // 綁定 btnAddSchedule
        Button btnAddSchedule = findViewById(R.id.btnAddSchedule);
        // 設置點擊事件
        btnAddSchedule.setOnClickListener(v -> {
            addSchedule();
        });
    }

    private void initDefaultTime() {
        EditText etStartTime = findViewById(R.id.etStartTime);

        // 禁止直接編輯
        etStartTime.setFocusable(false); // 禁止鍵盤輸入
        etStartTime.setClickable(true); // 允許點擊觸發事件

        // 為開始時間設置點擊事件，打開時間選擇對話框
        etStartTime.setOnClickListener(v -> {
            if (selectedClassId == -1 || classPeriod == 0) {
                Toast.makeText(this, "請先選擇課程", Toast.LENGTH_SHORT).show();
            } else {
                String currentTime = etStartTime.getText().toString(); // 獲取當前的開始時間
                showCustomTimePickerDialog(currentTime);
            }
        });

    }

    private void initDateSelectionButton() {
        Button btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void initCourseSelectionButton() {
        Button btnSelectCourse = findViewById(R.id.btnSelectCourse);
        btnSelectCourse.setOnClickListener(v -> showCourseSelectionDialog());
    }

    private void showDatePicker() {
        if (isDialogShow) return;
        isDialogShow = true;

        View dialogView = getLayoutInflater().inflate(R.layout.coach_scheduled_set_calendar, null);
        MultiCalendarView mCalendarView = dialogView.findViewById(R.id.ScheduledSet_calendarView);

        // 設置日期範圍
        new Thread(() -> {
            try {
                // 將教練編號轉換為 String
                String coachId = String.valueOf(Coach.getInstance().getCoachId());
                try (PreparedStatement statement = MyConnection.prepareStatement(
                        "SELECT 合約到期日 FROM 健身教練審核 WHERE 健身教練編號 = ?")) {
                    statement.setString(1, coachId);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        String contractEndDateStr = resultSet.getString("合約到期日");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date contractEndDate = dateFormat.parse(contractEndDateStr);

                        if (contractEndDate != null) {
                            long currentTime = System.currentTimeMillis();
                            long adjustedEndDate = contractEndDate.getTime() - (24 * 60 * 60 * 1000); // 扣一天
                            runOnUiThread(() -> mCalendarView.setDateRange(
                                    currentTime + (24 * 60 * 60 * 1000), // 起始日期：今天+1
                                    adjustedEndDate, // 結束日期：合約到期日 - 1天
                                    currentTime // 默認選中日期
                            ));
                        }
                    }
                    resultSet.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "無法設定日期範圍：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();



        // 如果有選中日期，回填
        if (dateInfoList != null) mCalendarView.setSelectedDateList(dateInfoList);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("選擇日期")
                .setView(dialogView)
                .setPositiveButton("確定", (dialog1, which) -> {
                    dateInfoList = mCalendarView.getSelectedDateList();

                    TextView summaryTextView = findViewById(R.id.tvSelectedDatesSummary);
                    if (dateInfoList == null || dateInfoList.isEmpty()) {
                        summaryTextView.setText("尚未選擇日期");
                    } else {
                        summaryTextView.setText(String.format("已選擇 %d 天", dateInfoList.size()));
                    }
                })
                .setNegativeButton("取消", null)
                .create();

        mCalendarView.setTag(dialog);
        dialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        dialog.show();
    }

    private void showCourseSelectionDialog() {
        Executors.newSingleThreadExecutor().execute(() -> {
            fetchCourseData();

            if (courseNames.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "無可用課程", Toast.LENGTH_SHORT).show());
                return;
            }

            runOnUiThread(() -> {
                String[] courseArray = courseNames.toArray(new String[0]);

                new AlertDialog.Builder(this)
                        .setTitle("選擇課程")
                        .setSingleChoiceItems(courseArray, selectedCourseIndex, (dialog, which) -> {
                            selectedCourseIndex = which; // 保存用戶選擇的索引
                        })
                        .setPositiveButton("確定", (dialog, which) -> {
                            String selectedCourse = courseArray[selectedCourseIndex];
                            int selectedClassId = courseIds.get(selectedCourseIndex);
                            updateSelectedCourse(selectedCourse, selectedClassId);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
        });
    }
    private void showCustomTimePickerDialog(String initialTime) {
        if (isDialogShow) return; // 避免重複開啟
        isDialogShow = true;

        // 加載自定義佈局
        View dialogView = getLayoutInflater().inflate(R.layout.coach_scheduled_add_ic_time_picker, null);

        NumberPicker npHour = dialogView.findViewById(R.id.npHour);
        NumberPicker npMinute = dialogView.findViewById(R.id.npMinute);

        // 設置小時範圍（6:00 ~ 22:30）
        npHour.setMinValue(6);
        npHour.setMaxValue(22);

        // 設置分鐘範圍（:00 和 :30）
        npMinute.setMinValue(0);
        npMinute.setMaxValue(1);
        npMinute.setDisplayedValues(new String[]{"00", "30"});

        // 解析傳入的初始時間
        int initialHour = 6;
        int initialMinute = 0;
        if (initialTime != null && !initialTime.isEmpty()) {
            try {
                String[] timeParts = initialTime.split(":");
                initialHour = Integer.parseInt(timeParts[0]);
                initialMinute = Integer.parseInt(timeParts[1]);
                initialMinute = (initialMinute == 30) ? 1 : 0; // 將分鐘轉換為 NumberPicker 的索引
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 設置 NumberPicker 的初始值
        npHour.setValue(initialHour);
        npMinute.setValue(initialMinute);

        // 建立 AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("選擇時間")
                .setPositiveButton("確定", (dialogInterface, which) -> {
                    int selectedHour = npHour.getValue();
                    int selectedMinute = npMinute.getValue() * 30;

                    // 更新開始時間
                    EditText etStartTime = findViewById(R.id.etStartTime);
                    EditText etEndTime = findViewById(R.id.etEndTime);

                    String startTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    etStartTime.setText(startTime);

                    String endTime = calculateTime(selectedHour, selectedMinute, classPeriod, true);
                    etEndTime.setText(endTime);
                })
                .setNegativeButton("取消", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        dialog.show();
    }

    private List<String> courseNames = new ArrayList<>();
    private List<Integer> courseIds = new ArrayList<>();

    private synchronized void fetchCourseData() {
        String query = "SELECT [課程編號], [課程名稱] FROM [健身教練課程] WHERE [健身教練編號] = ?";
        try (PreparedStatement statement = MyConnection.prepareStatement(query)) {
            statement.setInt(1, Coach.getInstance().getCoachId());
            ResultSet resultSet = statement.executeQuery();

            courseNames.clear();
            courseIds.clear();
            while (resultSet.next()) {
                courseIds.add(resultSet.getInt("課程編號"));
                courseNames.add(resultSet.getString("課程名稱"));
            }
        } catch (SQLException e) {
            Log.e("SQL", "課程查詢失敗", e);
        }
    }
    private void updateSelectedCourse(String selectedCourse, int classId) {
        TextView tvSelectedCourse = findViewById(R.id.tvSelectedCourse);
        tvSelectedCourse.setText(selectedCourse);
        selectedClassId = classId;

        // 獲取課程時長
        int classPeriod = getClassPeriod(classId);
        Log.e("SQL", "課程時間長度：" + classPeriod);
        if (classPeriod > 0) {
            // 保存課程時長到全局變數
            this.classPeriod = classPeriod;

            // 更新時間字段
            updateTimeFields(classPeriod);
        } else {
            Toast.makeText(this, "課程時長無效，請重新選擇課程", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateTimeFields(int classPeriod) {
        EditText etStartTime = findViewById(R.id.etStartTime);
        EditText etEndTime = findViewById(R.id.etEndTime);

        // 固定開始時間為 06:00
        String startTime = "06:00";
        etStartTime.setText(startTime);

        // 計算結束時間
        String endTime = calculateTime(6, 0, classPeriod, true);
        etEndTime.setText(endTime);
    }
    private String calculateTime(int hourOfDay, int minute, int classPeriod, boolean isStart) {
        int totalMinutes = hourOfDay * 60 + minute + (isStart ? classPeriod : -classPeriod);

        // 獲取開始和結束時間輸入框
        EditText etStartTime = findViewById(R.id.etStartTime);
        EditText etEndTime = findViewById(R.id.etEndTime);

        // 超出範圍的處理
        if (totalMinutes > 1380) { // 超過 23:00
            Toast.makeText(this, "結束時間不可超過 23:00", Toast.LENGTH_SHORT).show();
            if (isStart) {
                etStartTime.setText(""); // 清空開始時間
                etEndTime.setText(""); // 清空結束時間
            }
            return "";
        }
        if (totalMinutes < 360) { // 早於 06:00
            Toast.makeText(this, "時間不可早於 06:00", Toast.LENGTH_SHORT).show();
            if (!isStart) {
                etStartTime.setText(""); // 清空開始時間
                etEndTime.setText(""); // 清空結束時間
            }
            return "";
        }

        // 計算新時間
        int newHour = totalMinutes / 60;
        int newMinute = totalMinutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", newHour, newMinute);
    }

    private int getClassPeriod(Integer classId) {
        if (classId == null || classId == -1) return 0;

        try {
            Callable<Integer> task = () -> {
                int classPeriod = 0;
                try (PreparedStatement statement = MyConnection.prepareStatement(
                        "SELECT [課程時間長度] FROM [健身教練課程] WHERE [課程編號] = ?")) {
                    statement.setInt(1, classId);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) classPeriod = resultSet.getInt("課程時間長度");
                } catch (SQLException e) {
                    Log.e("SQL", "查詢課程時間失敗", e);
                }
                return classPeriod;
            };

            return Executors.newSingleThreadExecutor().submit(task).get();
        } catch (Exception e) {
            Log.e("FutureTask", "課程時間獲取失敗", e);
            return 0;
        }
    }

    private void addSchedule() {
        String courseStartTime = ((EditText) findViewById(R.id.etStartTime)).getText().toString();
        String courseEndTime = ((EditText) findViewById(R.id.etEndTime)).getText().toString();

        if (dateInfoList.isEmpty()) {
            Toast.makeText(this, "請先選擇日期再進行操作", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedClassId == -1) {
            Toast.makeText(this, "請先選擇課程再進行操作", Toast.LENGTH_SHORT).show();
            return;
        }

        String queryCheck = "SELECT 課程編號, 開始時間, 結束時間 " +
                "FROM [健身教練課表課程-判斷課程衝突用] " +
                "WHERE 日期 = ? AND 健身教練編號 = ? " +
                "ORDER BY 開始時間 ASC";

        String queryInsert = "INSERT INTO [健身教練課表] (課程編號, 日期, 開始時間, 結束時間, 星期幾, 預約人數) VALUES (?, ?, ?, ?, ?, ?)";

        Executors.newSingleThreadExecutor().execute(() -> {
            try (PreparedStatement checkStmt = MyConnection.prepareStatement(queryCheck);
                 PreparedStatement insertStmt = MyConnection.prepareStatement(queryInsert)) {

                String[] chineseDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};

                for (DateInfo dateInfo : dateInfoList) {
                    LocalDate localDate = LocalDate.of(dateInfo.getYear(), dateInfo.getMonth(), dateInfo.getDay());
                    String formattedDate = localDate.toString(); // 格式化為 yyyy-MM-dd
                    String dayOfWeek = chineseDays[localDate.getDayOfWeek().getValue() - 1]; // 轉換為中文星期

                    // 設置檢查衝突的參數
                    checkStmt.setString(1, formattedDate);
                    checkStmt.setInt(2, Coach.getInstance().getCoachId());

                    LocalTime selectedStartTime = LocalTime.parse(courseStartTime, DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime selectedEndTime = LocalTime.parse(courseEndTime, DateTimeFormatter.ofPattern("HH:mm"));

                    LocalTime previousEndTime = null;
                    LocalTime nextStartTime = null;

                    String previousCourseId = null;
                    String nextCourseId = null;

                    boolean hasConflict = false;

                    try (ResultSet resultSet = checkStmt.executeQuery()) {
                        while (resultSet.next()) {
                            LocalTime scheduledStartTime = LocalTime.parse(resultSet.getString("開始時間"), DateTimeFormatter.ofPattern("HH:mm"));
                            LocalTime scheduledEndTime = LocalTime.parse(resultSet.getString("結束時間"), DateTimeFormatter.ofPattern("HH:mm"));
                            String scheduledCourseId = resultSet.getString("課程編號");

                            // 判斷時間衝突
                            if ((selectedStartTime.isAfter(scheduledStartTime) && selectedStartTime.isBefore(scheduledEndTime)) ||
                                    (selectedEndTime.isAfter(scheduledStartTime) && selectedEndTime.isBefore(scheduledEndTime)) ||
                                    (selectedStartTime.equals(scheduledStartTime) || selectedEndTime.equals(scheduledEndTime) ||
                                            (selectedStartTime.isBefore(scheduledStartTime) && selectedEndTime.isAfter(scheduledEndTime)))) {
                                hasConflict = true;
                                break;
                            }

                            // 更新最近的前一課程結束時間
                            if (!scheduledEndTime.isAfter(selectedStartTime) &&
                                    (previousEndTime == null || scheduledEndTime.isAfter(previousEndTime))) {
                                previousEndTime = scheduledEndTime;
                                previousCourseId = scheduledCourseId;
                            }

                            // 更新最近的後一課程開始時間
                            if (!scheduledStartTime.isBefore(selectedEndTime) &&
                                    (nextStartTime == null || scheduledStartTime.isBefore(nextStartTime))) {
                                nextStartTime = scheduledStartTime;
                                nextCourseId = scheduledCourseId;
                            }
                        }

                        // 檢查與前後課程的間隔時間
                        if (previousEndTime != null && !previousCourseId.equals(String.valueOf(selectedClassId)) &&
                                selectedStartTime.isBefore(previousEndTime.plusMinutes(30))) {
                            runOnUiThread(() -> Toast.makeText(this, "請與前課程預留至少30分鐘交通時間", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        if (nextStartTime != null && !nextCourseId.equals(String.valueOf(selectedClassId)) &&
                                selectedEndTime.isAfter(nextStartTime.minusMinutes(30))) {
                            runOnUiThread(() -> Toast.makeText(this, "請與後課程預留至少30分鐘交通時間", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        if (hasConflict) {
                            runOnUiThread(() -> Toast.makeText(this, "所選時間與現有時間重疊", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }

                    Log.d("SQL", "queryCheck: 日期=" + formattedDate + ", 健身教練編號=" + Coach.getInstance().getCoachId());
                    Log.d("SQL", "queryInsert: 課程編號=" + selectedClassId + ", 日期=" + formattedDate + ", 開始時間=" + courseStartTime + ", 結束時間=" + courseEndTime + ", 星期幾=" + dayOfWeek);

                    // 插入資料
                    insertStmt.setInt(1, selectedClassId); // 課程編號
                    insertStmt.setString(2, formattedDate); // 日期
                    insertStmt.setString(3, courseStartTime); // 開始時間
                    insertStmt.setString(4, courseEndTime); // 結束時間
                    insertStmt.setString(5, dayOfWeek); // 星期幾
                    insertStmt.setInt(6, 0); // 預約人數（默認為 0）
                    insertStmt.executeUpdate();
                }

                // 成功提示
                runOnUiThread(() -> {
                    Toast.makeText(this, "班表已新增成功！", Toast.LENGTH_SHORT).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Intent intent = new Intent(this, ScheduledMain.class);
                        startActivity(intent);
                        finish();
                    }, 2000);
                });

            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "新增班表失敗", Toast.LENGTH_SHORT).show());
            }
        });
    }

}

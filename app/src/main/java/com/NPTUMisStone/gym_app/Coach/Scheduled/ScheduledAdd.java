package com.NPTUMisStone.gym_app.Coach.Scheduled;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class ScheduledAdd extends AppCompatActivity {

    List<DateInfo> dateInfoList;
    boolean isDialogShow = false;
    Connection MyConnection;

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
        findViewById(R.id.ScheduledAdd_backButton).setOnClickListener(v -> finish());

        // 初始化按鈕和輸入框
        initDateSelectionButton();
        initCourseSelectionButton();
    }
    private void initDateSelectionButton() {
        Button btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void initCourseSelectionButton() {
        Button btnSelectCourse = findViewById(R.id.btnSelectCourse);
        btnSelectCourse.setOnClickListener(v -> showCourseSelectionDialog());
    }

    private void initTimePickerEditText(int classPeriod) {
        EditText etStartTime = findViewById(R.id.etStartTime);
        etStartTime.setOnClickListener(v -> {
            if (selectedClassId == -1) {
                Toast.makeText(this, "請先選擇課程", Toast.LENGTH_SHORT).show();
                return;
            }
            showTimePickerDialog(classPeriod, true);
        });
    }


    private void showDatePicker() {
        if (isDialogShow) return;    //避免重複開啟
        isDialogShow = true;
        View dialogView = getLayoutInflater().inflate(R.layout.coach_scheduled_set_calendar, null); //自定義日歷元件：https://blog.csdn.net/coffee_shop/article/details/130709029
        MultiCalendarView mCalendarView = dialogView.findViewById(R.id.ScheduledSet_calendarView);
        mCalendarView.setDateRange(System.currentTimeMillis(), System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000, System.currentTimeMillis());
        if (dateInfoList != null) mCalendarView.setSelectedDateList(dateInfoList);
        mCalendarView.setOnMultiDateSelectedListener((view, selectedDays, selectedDates) -> {
            ((AlertDialog) view.getTag()).setMessage(selectedDates.isEmpty() ? "請選擇日期" : "已選擇" + selectedDates.size() + "天");
            return null;
        });
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(dateInfoList == null || dateInfoList.isEmpty() ? "請選擇日期" : "已選擇" + dateInfoList.size() + "天")
                .setTitle("選擇日期").setView(dialogView).setPositiveButton("確定", (dialog1, which) -> {
                    dateInfoList = mCalendarView.getSelectedDateList();
                    List<String> dateList = new ArrayList<>();
                    for (DateInfo dateInfo : dateInfoList)
                        dateList.add(dateInfo.getYear() + "/" + dateInfo.getMonth() + "/" + dateInfo.getDay());
                    ((TextView) findViewById(R.id.tvSelectedDates)).setText(dateList.isEmpty() ? "" : dateList.toString());
                }).create();
        mCalendarView.setTag(dialog);
        dialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        dialog.show();
    }
    private void showCourseSelectionDialog() {
        Executors.newSingleThreadExecutor().execute(() -> {
            fetchCourseData(); // 從後端獲取課程名和 ID 列表

            if (courseNames.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "無可用課程", Toast.LENGTH_SHORT).show());
                return;
            }

            String[] courseArray = courseNames.toArray(new String[0]);
            runOnUiThread(() -> {
                final int[] selectedItem = {0}; // 默認選擇第一項

                // 創建 AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("選擇課程")
                        .setSingleChoiceItems(courseArray, selectedItem[0], (dialog, which) -> {
                            selectedItem[0] = which; // 更新選擇的課程索引
                        })
                        .setPositiveButton("確定", (dialog, which) -> {
                            String selectedCourse = courseArray[selectedItem[0]];
                            int selectedClassId = courseIds.get(selectedItem[0]); // 對應的 classId
                            updateSelectedCourse(selectedCourse, selectedClassId); // 更新主界面
                            dialog.dismiss();
                        })
                        .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            });
        });
    }


    private List<String> courseNames = new ArrayList<>();
    private List<Integer> courseIds = new ArrayList<>();

    private synchronized void fetchCourseData() {
        String query = "SELECT [課程編號], [課程名稱] FROM [健身教練課程] WHERE [健身教練編號] = ?";

        try (PreparedStatement statement = MyConnection.prepareStatement(query)) {
            statement.setInt(1, Coach.getInstance().getCoachId()); // 使用教練 ID 過濾課程
            ResultSet resultSet = statement.executeQuery();

            courseNames.clear();
            courseIds.clear();

            // 獲取課程名稱和對應的課程 ID
            while (resultSet.next()) {
                courseIds.add(resultSet.getInt("課程編號"));
                courseNames.add(resultSet.getString("課程名稱"));
            }
        } catch (SQLException e) {
            Log.e("SQL", "課程查詢失敗", e);
        }
    }

    private int selectedClassId = -1; // 保存選中的 classId

    private void updateSelectedCourse(String selectedCourse, int classId) {
        TextView tvSelectedCourse = findViewById(R.id.tvSelectedCourse);
        tvSelectedCourse.setText(selectedCourse);
        selectedClassId = classId; // 保存選中的 classId
        Toast.makeText(this, "選擇的課程：" + selectedCourse, Toast.LENGTH_SHORT).show();
        int classPeriod = getClassPeriod(selectedClassId); // 使用選中的 classId 獲取課程時長
        initTimePickerEditText(classPeriod);
    }

    private void showTimePickerDialog(int classPeriod, boolean isStart) {
        if (isDialogShow) return;
        isDialogShow = true;

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            // 驗證時間
            EditText startTimeEditText = findViewById(R.id.etStartTime);
            EditText endTimeEditText = findViewById(R.id.etEndTime);

            if (isStart && hourOfDay < 6) {
                Toast.makeText(this, "開始時間不得在06:00前", Toast.LENGTH_SHORT).show();
                // 清空時間
                startTimeEditText.setText("");
                endTimeEditText.setText("");
                return;
            }

            if (!isStart && hourOfDay > 23) {
                Toast.makeText(this, "結束時間不得超過23:00點", Toast.LENGTH_SHORT).show();
                // 清空時間
                startTimeEditText.setText("");
                endTimeEditText.setText("");
                return;
            }

            if (isStart) {
                // 更新開始時間
                String startTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                startTimeEditText.setText(startTime);

                // 根據開始時間計算結束時間
                String endTime = calculateTime(hourOfDay, minute, classPeriod, true);

                // 檢查結束時間是否超過 23:00
                int endHour = Integer.parseInt(endTime.split(":")[0]);
                if (endHour > 23) {
                    Toast.makeText(this, "課程結束時間不得超過十一點", Toast.LENGTH_SHORT).show();
                    // 清空時間
                    startTimeEditText.setText("");
                    endTimeEditText.setText("");
                } else {
                    endTimeEditText.setText(endTime);
                }
            } else {
                // 更新結束時間
                String endTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                endTimeEditText.setText(endTime);

                // 根據結束時間計算開始時間
                String startTime = calculateTime(hourOfDay, minute, classPeriod, false);

                // 檢查開始時間是否小於 7:00
                int startHour = Integer.parseInt(startTime.split(":")[0]);
                if (startHour < 7) {
                    Toast.makeText(this, "課程開始時間不得早於七點", Toast.LENGTH_SHORT).show();
                    // 清空時間
                    startTimeEditText.setText("");
                    endTimeEditText.setText("");
                } else {
                    startTimeEditText.setText(startTime);
                }
            }
        }, 7, 0, true); // 預設時間為早上 7:00

        timePickerDialog.setOnDismissListener(dialogInterface -> isDialogShow = false);
        timePickerDialog.show();
    }

    private String calculateTime(int hourOfDay, int minute, int classPeriod, boolean isStart) {
        // 將時長轉換為分鐘加總
        int totalMinutes = hourOfDay * 60 + minute;

        // 加或減去課程時長
        totalMinutes += isStart ? classPeriod : -classPeriod;

        // 確保時間在有效範圍內
        if (totalMinutes < 0) totalMinutes += 24 * 60; // 一天的總分鐘數

        // 計算小時和分鐘
        int newHour = (totalMinutes / 60) % 24; // 小時部分
        int newMinute = totalMinutes % 60;     // 分鐘部分

        // 返回格式化時間字串
        return String.format(Locale.getDefault(), "%02d:%02d", newHour, newMinute);
    }



    //取得課程時間長度
    private int getClassPeriod(Integer classId){
        if (classId == null || classId == -1) {
            Log.w("ScheduledAdd", "Invalid classId: " + classId);
            return 0; // 默認為 0
        }
        Callable<Integer> task = () -> {
            int classPeriod = 0;
            try {
                String searchQuery = "SELECT [課程時間長度] FROM [健身教練課程] WHERE [課程編號] = ?";
                PreparedStatement searchStatement = MyConnection.prepareStatement(searchQuery);
                searchStatement.setInt(1, classId);
                ResultSet searchResult = searchStatement.executeQuery();
                if (searchResult.next()) classPeriod = searchResult.getInt("課程時間長度");
            } catch (SQLException e) {
                Log.e("SQL", "Error in search SQL", e);
            }
            if (classPeriod == 0) new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(this, "取得課程時間長度失敗", Toast.LENGTH_SHORT).show());
            return classPeriod;
        };
        try {
            return Executors.newSingleThreadExecutor().submit(task).get();
        } catch (Exception e) {
            Log.e("FutureTask", "Error getting class period", e);
            return 0;
        }
    }
}
package com.NPTUMisStone.gym_app.Coach.Main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.NPTUMisStone.gym_app.Coach.Class.ClassMain;
import com.NPTUMisStone.gym_app.Coach.Comments.Coach_Comments;

import com.NPTUMisStone.gym_app.Coach.Records.All;
import com.NPTUMisStone.gym_app.Coach.Records.Detail;
import com.NPTUMisStone.gym_app.Coach.Scheduled.ScheduledMain;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ProgressBarHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.Executors;


public class CoachHome extends AppCompatActivity {

    private Connection MyConnection;
    private ProgressBarHandler progressBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_main_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.CoachHome_constraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 初始化其他屬性
        MyConnection = new SQLConnection(findViewById(R.id.CoachHome_constraintLayout)).IWantToConnection();
        progressBarHandler = new ProgressBarHandler(this, findViewById(android.R.id.content));

        // 初始化教練資訊
        initCoachInfo();
        // 下拉刷新處理
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.CoachHome_swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshPageContent();
            swipeRefreshLayout.setRefreshing(false);
        });

        // 初次載入最近課程
        fetchClosestUpcomingAppointment();
    }

    private void refreshPageContent() {
        fetchClosestUpcomingAppointment();
    }

    private void initCoachInfo() {
        ((TextView) findViewById(R.id.CoachHome_nameText)).setText(getGreetingMessage());
        findViewById(R.id.CoachHome_photoImage).setOnClickListener(v -> startActivity(new Intent(this, CoachInfo.class)));
        setUserImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyConnection == null) {
            MyConnection = new SQLConnection(findViewById(R.id.CoachHome_constraintLayout)).IWantToConnection();
            if (MyConnection == null) {
                Log.e("Database", "資料庫連線失敗");
                progressBarHandler.hideProgressBar();
                return;
            }
        }
        setUserImage(); // 確保刷新時設置圖片
        fetchClosestUpcomingAppointment();
    }


    private String getGreetingMessage() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 6) greeting = "🌙 凌晨，該休息了";
        else if (hour < 12) greeting = "☀️ 早上好";
        else if (hour < 18) greeting = "🌤️ 下午好";
        else greeting = "🌙 晚上好";
        return getString(R.string.All_welcome, greeting, Coach.getInstance().getCoachName());
    }

    private void setUserImage() {
        byte[] image = Coach.getInstance().getCoachImage();
        ImageView photoImageView = findViewById(R.id.CoachHome_photoImage);

        if (image != null && image.length > 0) {
            // 將圖片轉換為 Bitmap 並設置
            photoImageView.setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
        } else {
            // 如果圖片為空，設置默認圖片
            photoImageView.setImageResource(R.drawable.coach_main_ic_default);
        }
    }


    public void onClick(View view) {
        if (progressBarHandler.isLoading()) return;
        progressBarHandler.showProgressBar();
        int id = view.getId();
        try {
            if (id == R.id.CoachHome_viewAppointmentsCard)
                startActivity(new Intent(this, All.class));
            else if (id == R.id.CoachHome_viewScheduleCard)
                startActivity(new Intent(this, ScheduledMain.class));
            else if (id == R.id.CoachHome_classMaintenanceCard)
                startActivity(new Intent(this, ClassMain.class));
            else if (id == R.id.CoachHome_commentManagementCard)
                startActivity(new Intent(this, Coach_Comments.class));
        } catch (Exception e) {
            Log.e("Button", "Button click error", e);
        }
    }
    private boolean isLoading = false;

    private void fetchClosestUpcomingAppointment() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String sql = "SELECT TOP 1 * FROM 健身教練課表課程合併 " +
                        "WHERE 健身教練編號 = ? AND " +
                        "(日期 > ? OR (日期 = ? AND 結束時間 > ?)) " +
                        "ORDER BY 日期 ASC, 開始時間 ASC";

                if (MyConnection == null || MyConnection.isClosed()) {
                    Log.e("Database", "資料庫連線失敗");
                    new Handler(Looper.getMainLooper()).post(() -> {
                        progressBarHandler.hideProgressBar();
                        isLoading = false;
                    });
                    return;
                }

                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                java.sql.Time currentTime = new java.sql.Time(System.currentTimeMillis());
                searchStatement.setInt(1, Coach.getInstance().getCoachId());
                searchStatement.setDate(2, today);
                searchStatement.setDate(3, today);
                searchStatement.setTime(4, currentTime);

                ResultSet rs = searchStatement.executeQuery();

                if (rs.next()) {
                    int scheduleID = rs.getInt("課表編號"); // 獲取 ScheduleID
                    String courseName = rs.getString("課程名稱");
                    String date = rs.getDate("日期").toString();
                    String dayOfWeek = rs.getString("星期幾");
                    String startTime = rs.getString("開始時間");
                    String endTime = rs.getString("結束時間");
                    String locationName = rs.getString("地點名稱");
                    int locationType = rs.getInt("地點類型");
                    String city = rs.getString("縣市");
                    String district = rs.getString("行政區");
                    int bookedPeople = rs.getInt("預約人數");
                    int totalPeople = rs.getInt("上課人數");

                    new Handler(Looper.getMainLooper()).post(() -> {
                        updateUIWithClosestUpcomingAppointment(scheduleID, courseName, date, dayOfWeek, startTime, endTime, locationName, locationType, city, district, bookedPeople, totalPeople);
                        progressBarHandler.hideProgressBar();
                        isLoading = false;
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        updateUIWithNoUpcomingAppointment();
                        progressBarHandler.hideProgressBar();
                        isLoading = false;
                    });
                }

                rs.close();
                searchStatement.close();
            } catch (SQLException e) {
                Log.e("SQL", "資料庫查詢失敗", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBarHandler.hideProgressBar();
                    isLoading = false;
                });
            } catch (Exception e) {
                Log.e("Exception", "發生意外錯誤", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBarHandler.hideProgressBar();
                    isLoading = false;
                });
            }
        });
    }

    private void updateUIWithClosestUpcomingAppointment(int scheduleID, String courseName, String date, String dayOfWeek,
                                                        String startTime, String endTime, String locationName,
                                                        int locationType, String city, String district, int bookedPeople, int totalPeople) {
        TextView classTypeLabel = findViewById(R.id.classTypeLabel);
        TextView textCourseName = findViewById(R.id.textCourseName);
        TextView textDate = findViewById(R.id.textDate);
        TextView textTime = findViewById(R.id.textTime);
        TextView textLocation = findViewById(R.id.textLocation);
        TextView peopleCount = findViewById(R.id.peopleCount);
        View classDetailLayout = findViewById(R.id.class_detailLayout); // 找到 class_detailLayout

        // 根據 locationType 設置標籤
        if (locationType == 2) { // 到府服務
            classTypeLabel.setText("到府課程");
            classTypeLabel.setBackgroundResource(R.drawable.class_type_label_bg); // 藍底
        } else { // 團體課程
            classTypeLabel.setText("團體課程");
            classTypeLabel.setBackgroundResource(R.drawable.class_type_label_red_bg); // 紅底
        }

        // 設置其他課程資訊
        textCourseName.setText("\uD83D\uDCCC"+courseName);
        textDate.setText(String.format("%s (%s)", date, dayOfWeek));
        textTime.setText(String.format("%s - %s", startTime, endTime));
        textLocation.setText(locationName);
        peopleCount.setText(bookedPeople + " / " + totalPeople);

        // 切換背景為 "有課程" 圖片
        ConstraintLayout layout = findViewById(R.id.classbackground);
        layout.setBackgroundResource(R.drawable.coach_home_hasclass);

        // 顯示課程詳細信息布局
        if (classDetailLayout != null) {
            classDetailLayout.setVisibility(View.VISIBLE);
        }

        // 設置按鈕點擊事件
        Button viewAppointmentListButton = findViewById(R.id.viewAppointmentListButton);
        viewAppointmentListButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, Detail.class);
            intent.putExtra("看預約名單ID", scheduleID);
            startActivity(intent);
        });
    }


    private void updateUIWithNoUpcomingAppointment() {
        TextView textCourseName = findViewById(R.id.textCourseName);
        TextView textDate = findViewById(R.id.textDate);
        TextView textTime = findViewById(R.id.textTime);
        TextView textLocation = findViewById(R.id.textLocation);
        TextView peopleCount = findViewById(R.id.peopleCount);
        TextView classTypeLabel = findViewById(R.id.classTypeLabel);
        Button viewAppointmentListButton = findViewById(R.id.viewAppointmentListButton);
        View classDetailLayout = findViewById(R.id.class_detailLayout); // 找到 class_detailLayout

        // 清空課程資訊
        textCourseName.setText("");
        textDate.setText("");
        textTime.setText("");
        textLocation.setText("");
        peopleCount.setText("0 / 0");
        classTypeLabel.setText("");
        classTypeLabel.setBackgroundResource(0); // 移除標籤背景

        // 隱藏按鈕
        viewAppointmentListButton.setVisibility(View.GONE);

        // 隱藏課程詳細信息布局
        if (classDetailLayout != null) {
            classDetailLayout.setVisibility(View.GONE);
        }

        // 切換背景為 "無課程" 圖片
        ConstraintLayout layout = findViewById(R.id.classbackground);
        layout.setBackgroundResource(R.drawable.coach_home_default);
    }
}

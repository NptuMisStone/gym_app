package com.NPTUMisStone.gym_app.Coach.Main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.NPTUMisStone.gym_app.Coach.Class.ClassMain;
import com.NPTUMisStone.gym_app.Coach.Comments.Coach_Comments;

import com.NPTUMisStone.gym_app.Coach.Records.All;
import com.NPTUMisStone.gym_app.Coach.Scheduled.ScheduledMain;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.Map.Maps;
import com.NPTUMisStone.gym_app.User_And_Coach.Map.Class;
import com.NPTUMisStone.gym_app.User_And_Coach.ProgressBarHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.Executors;


public class CoachHome extends AppCompatActivity {

    private Connection MyConnection;
    private ProgressBarHandler progressBarHandler;

    // UI 元件
    private View upcomingClassCard;
    private TextView upcomingClassTitle;
    private TextView upcomingClassDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coach_main_home);

        // 初始化 UI 元件
        upcomingClassCard = findViewById(R.id.CoachHome_upcomingClassCard);
        upcomingClassTitle = findViewById(R.id.CoachHome_upcomingClassTitle);
        upcomingClassDetails = findViewById(R.id.CoachHome_upcomingClassDetails);

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
            photoImageView.setImageResource(R.drawable.coach_default);
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
                    String courseName = rs.getString("課程名稱");
                    String date = rs.getDate("日期").toString();
                    String dayOfWeek = rs.getString("星期幾");
                    String startTime = rs.getString("開始時間");
                    String endTime = rs.getString("結束時間");
                    String locationName = rs.getString("地點名稱");
                    int locationType = rs.getInt("地點類型");
                    String city = rs.getString("縣市");
                    String district = rs.getString("行政區");

                    new Handler(Looper.getMainLooper()).post(() -> {
                        updateUIWithClosestUpcomingAppointment(courseName, date, dayOfWeek, startTime, endTime, locationName, locationType, city, district);
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

    private void updateUIWithClosestUpcomingAppointment(String courseName, String date, String dayOfWeek,
                                                        String startTime, String endTime, String locationName,
                                                        int locationType, String city, String district) {
        // 設置地點
        String locationText;
        if (locationType == 2) { // 到府服務
            locationText = "到府 (" + city + district + ")";
        } else { // 固定地點
            locationText = locationName;
        }

        // 更新 UI 顯示
        upcomingClassCard.setVisibility(View.VISIBLE);
        upcomingClassTitle.setText("即將到來的課程");
        upcomingClassDetails.setText(String.format(
                "課程名稱: %s\n日期: %s (%s)\n時間: %s - %s\n地點: %s",
                courseName, date, dayOfWeek, startTime, endTime, locationText
        ));

        // 根據地點類型設置背景顏色
        if (locationType == 2) { // 到府服務
            upcomingClassCard.setBackgroundResource(R.drawable.course_card_blue);
        } else { // 固定地點
            upcomingClassCard.setBackgroundResource(R.drawable.course_card_red);
        }
    }


    private void updateUIWithNoUpcomingAppointment() {
        upcomingClassCard.setVisibility(View.VISIBLE);
        upcomingClassTitle.setText("沒有即將到來的課程");
        upcomingClassDetails.setText("未來沒有課程安排");
    }

}

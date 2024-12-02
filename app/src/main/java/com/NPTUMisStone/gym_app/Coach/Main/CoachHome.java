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

import com.NPTUMisStone.gym_app.Coach.Records.Coach_AppointmentsAll;
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
import java.util.Objects;
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

        // 設置按鈕點擊事件
        findViewById(R.id.CoachHome_testButton1).setOnClickListener(v -> startActivity(new Intent(this, Maps.class)));
        findViewById(R.id.CoachHome_testButton2).setOnClickListener(v -> startActivity(new Intent(this, Class.class)));

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
        if (image != null) {
            ((ImageView) findViewById(R.id.CoachHome_photoImage)).setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
        }
    }

    public void onClick(View view) {
        if (progressBarHandler.isLoading()) return;
        progressBarHandler.showProgressBar();
        int id = view.getId();
        try {
            if (id == R.id.CoachHome_viewAppointmentsCard)
                startActivity(new Intent(this, Coach_AppointmentsAll.class));
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
    private void fetchClosestUpcomingAppointment() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 修改後的 SQL 查詢語句
                String sql = "SELECT TOP 1 * FROM 健身教練課表課程合併 " +
                        "WHERE 健身教練編號 = ? AND 結束時間 > ? " +
                        "ORDER BY 開始時間";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);

                // 設定查詢參數
                searchStatement.setInt(1, Coach.getInstance().getCoachId());
                java.sql.Time currentTime = new java.sql.Time(System.currentTimeMillis());
                searchStatement.setTime(2, currentTime);

                ResultSet rs = searchStatement.executeQuery();

                if (rs.next()) {
                    // 從 SQL 結果集中直接取得資料
                    String courseName = rs.getString("課程名稱");
                    String date = rs.getDate("日期").toString();
                    String dayOfWeek = rs.getString("星期幾");
                    String startTime = rs.getString("開始時間");
                    String endTime = rs.getString("結束時間");
                    String locationName = rs.getString("地點名稱");
                    String locationType = rs.getString("地點類型");

                    // 更新 UI
                    new Handler(Looper.getMainLooper()).post(() -> updateUIWithClosestAppointment(
                            courseName, date, dayOfWeek, startTime, endTime, locationName, locationType
                    ));
                } else {
                    // 沒有課程時更新 UI
                    new Handler(Looper.getMainLooper()).post(this::updateUIWithNoAppointment);
                }

                rs.close();
                searchStatement.close();
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
                new Handler(Looper.getMainLooper()).post(() -> progressBarHandler.hideProgressBar());
            }
        });
    }

    private void updateUIWithClosestAppointment(String courseName, String date, String dayOfWeek,
                                                String startTime, String endTime, String locationName,
                                                String locationType) {
        upcomingClassCard.setVisibility(View.VISIBLE);
        upcomingClassTitle.setText("即將到來的課程");
        upcomingClassDetails.setText(String.format(
                "課程名稱: %s\n日期: %s (%s)\n時間: %s - %s\n地點: %s (%s)",
                courseName, date, dayOfWeek, startTime, endTime, locationName, locationType
        ));
    }

    private void updateUIWithNoAppointment() {
        upcomingClassCard.setVisibility(View.VISIBLE);
        upcomingClassTitle.setText("沒有即將到來的課程");
        upcomingClassDetails.setText("未來沒有課程安排");
    }

}

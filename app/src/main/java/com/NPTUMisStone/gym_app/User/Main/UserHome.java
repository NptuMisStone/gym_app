package com.NPTUMisStone.gym_app.User.Main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Coach.Records.Detail;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Class.ClassList;
import com.NPTUMisStone.gym_app.User.Coach.CoachList;
import com.NPTUMisStone.gym_app.User.Like.UserLike;
import com.NPTUMisStone.gym_app.User.Records.Appointment;
import com.NPTUMisStone.gym_app.User.Records.Fragment.NowFragment;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.AdHelper;
import com.NPTUMisStone.gym_app.User_And_Coach.UI.Contact;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.Map.View;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ProgressBarHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class UserHome extends AppCompatActivity {
    private Connection MyConnection;
    ProgressBarHandler progressBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_main_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.UserHome_constraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 初始化其他屬性
        MyConnection = new SQLConnection(findViewById(R.id.UserHome_constraintLayout)).IWantToConnection();
        progressBarHandler = new ProgressBarHandler(this, findViewById(android.R.id.content));
        init_userinfo();

        // 下拉刷新處理
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.UserHome_swipeRefreshLayout);
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

    private void init_userinfo() {
        ((TextView) findViewById(R.id.UserHome_nameText)).setText(getGreetingMessage());
        findViewById(R.id.UserHome_photoImage).setOnClickListener(v -> startActivity(new Intent(this, UserInfo.class)));
        registerReceiver(broadcastReceiver, new IntentFilter("com.NPTUMisStone.gym_app.LOGOUT"), Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Context.RECEIVER_NOT_EXPORTED : 0);
        setUserImage();
    }

    private String getGreetingMessage() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 6) greeting = "🌙 凌晨，該休息了";
        else if (hour < 12) greeting = "☀️ 早上好";
        else if (hour < 18) greeting = "🌤️ 下午好";
        else greeting = "🌙 晚上好";
        return getString(R.string.All_welcome, greeting, User.getInstance().getUserName());
    }

    private void setUserImage() {
        Executors.newSingleThreadExecutor().execute(() -> {
            byte[] image = User.getInstance().getUserImage();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (image != null) {
                    ((ImageView) findViewById(R.id.UserHome_photoImage)).setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
                }
            });
        });
    }
    //【《Android》『呼叫外部 App』- 透過 startActivity 執行外部 App 的基本方法】：
    // https://xnfood.com.tw/android-call-app-startactivity/
    //(可參考)：Android—组件化的搭建：https://www.cnblogs.com/wang66a/p/17769227.html
    public void onClick(android.view.View view) {
        if (progressBarHandler.isLoading()) return;
        progressBarHandler.showProgressBar();
        int id = view.getId();
        try {
            if (id == R.id.UserHome_classCard) {
                startActivity(new Intent(this, ClassList.class));
            } else if (id == R.id.UserHome_coachCard) {
                startActivity(new Intent(this, CoachList.class));
            } else if (id == R.id.UserHome_loveCard) {
                startActivity(new Intent(this, UserLike.class));
            } else if (id == R.id.UserHome_historyCard) {
                startActivity(new Intent(this, Appointment.class));
            } else if (id == R.id.UserHome_gymCard) {
                startActivity(new Intent(this, View.class));
            } else if (id == R.id.UserHome_contactCard) {
                startActivity(new Intent(this, Contact.class));
            }
        } catch (Exception e) {
            Log.e("Button", "Button click error", e);
        } finally {
            progressBarHandler.hideProgressBar();
        }
    }

    //為了要在登出時關閉Home頁面，註冊廣播器
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (MyConnection == null) {
            MyConnection = new SQLConnection(findViewById(R.id.UserHome_constraintLayout)).IWantToConnection();
            if (MyConnection == null) {
                Log.e("Database", "資料庫連線失敗");
                progressBarHandler.hideProgressBar();
                return;
            }
        }
        setUserImage(); // 確保刷新時設置圖片
        fetchClosestUpcomingAppointment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private boolean isLoading = false;

    private void fetchClosestUpcomingAppointment() {
        if (isLoading) {
            return;
        }
        isLoading = true;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String sql = "SELECT TOP 1 * FROM [使用者預約-有預約的] " +
                        "WHERE 使用者編號 = ? AND " +
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
                searchStatement.setInt(1, User.getInstance().getUserId());
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
                    int bookedPeople = rs.getInt("預約人數");
                    int totalPeople = rs.getInt("上課人數");

                    new Handler(Looper.getMainLooper()).post(() -> {
                        updateUIWithClosestUpcomingAppointment(courseName, date, dayOfWeek, startTime, endTime, locationName, locationType, city, district, bookedPeople, totalPeople);
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
                                                        int locationType, String city, String district, int bookedPeople, int totalPeople) {
        TextView classTypeLabel = findViewById(R.id.classTypeLabel);
        TextView textCourseName = findViewById(R.id.textCourseName);
        TextView textDate = findViewById(R.id.textDate);
        TextView textTime = findViewById(R.id.textTime);
        TextView textLocation = findViewById(R.id.textLocation);
        TextView peopleCount = findViewById(R.id.peopleCount);
        android.view.View classDetailLayout = findViewById(R.id.class_detailLayout); // 找到 class_detailLayout

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
        layout.setBackgroundResource(R.drawable.user_home_hasclass);

        // 顯示課程詳細信息布局
        if (classDetailLayout != null) {
            classDetailLayout.setVisibility(android.view.View.VISIBLE);
        }

        // 設置按鈕點擊事件
        Button viewAppointmentListButton = findViewById(R.id.viewAppointmentListButton);
        viewAppointmentListButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, Appointment.class);
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
        android.view.View classDetailLayout = findViewById(R.id.class_detailLayout); // 找到 class_detailLayout

        // 清空課程資訊
        textCourseName.setText("");
        textDate.setText("");
        textTime.setText("");
        textLocation.setText("");
        peopleCount.setText("0 / 0");
        classTypeLabel.setText("");
        classTypeLabel.setBackgroundResource(0); // 移除標籤背景

        // 隱藏按鈕
        viewAppointmentListButton.setVisibility(android.view.View.GONE);

        // 隱藏課程詳細信息布局
        if (classDetailLayout != null) {
            classDetailLayout.setVisibility(android.view.View.GONE);
        }

        // 切換背景為 "無課程" 圖片
        ConstraintLayout layout = findViewById(R.id.classbackground);
        layout.setBackgroundResource(R.drawable.user_home_default);
    }
}
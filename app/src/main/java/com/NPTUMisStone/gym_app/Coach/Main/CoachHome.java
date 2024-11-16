package com.NPTUMisStone.gym_app.Coach.Main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.NPTUMisStone.gym_app.Coach.Class.ClassMain;
import com.NPTUMisStone.gym_app.Coach.Comments.Coach_Comments;
import com.NPTUMisStone.gym_app.Coach.Records.BookingList;
import com.NPTUMisStone.gym_app.Coach.Records.BookingDetail;
import com.NPTUMisStone.gym_app.Coach.Scheduled.ScheduledMain;
import com.NPTUMisStone.gym_app.Coach.Scheduled.ScheduledSet;
import com.NPTUMisStone.gym_app.Coach.Scheduled.ScheduledCheck;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.AIInteractive;
import com.NPTUMisStone.gym_app.User_And_Coach.Advertisement;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.ProgressBarHandler;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class CoachHome extends AppCompatActivity {
    Connection MyConnection;
    List<Advertisement> adList = new ArrayList<>();
    ProgressBarHandler progressBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_main_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        progressBarHandler = new ProgressBarHandler(this, findViewById(android.R.id.content));
        init_coachInfo();
        //getString();
    }

    private void init_coachInfo() {
        ((TextView) findViewById(R.id.CoachHome_nameText)).setText(getGreetingMessage());
        ((TextView) findViewById(R.id.CoachHome_idText)).setText(getString(R.string.All_idText, Coach.getInstance().getCoachId()));
        findViewById(R.id.CoachHome_editButton).setOnClickListener(v -> startActivity(new Intent(this, CoachInfo.class)));
        findViewById(R.id.CoachHome_photoImage).setOnClickListener(v -> startActivity(new Intent(this, CoachInfo.class)));
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
        return getString(R.string.All_welcome, greeting, Coach.getInstance().getCoachName());
    }

    private void setUserImage() {
        byte[] image = Coach.getInstance().getCoachImage(); //將byte[]轉換成Bitmap：https://stackoverflow.com/questions/3520019/display-image-from-bytearray
        if (image != null)
            ((ImageView) findViewById(R.id.CoachHome_photoImage)).setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
    }

    public void onClick(View view) {
        if (progressBarHandler.isLoading()) return;
        progressBarHandler.showProgressBar();
        int id = view.getId();
        try {
            if (id == R.id.CoachHome_viewAppointmentsCard)
                startActivity(new Intent(this, BookingDetail.class));
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
        progressBarHandler.hideProgressBar();
        init_coachInfo();
        Log.d("ActivityState", "Activity resumed, progress bar hidden.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }/*
    private void getString() { //API連接測試
        String url = "https://webapplication320240825010511.azurewebsites.net/api/MyApi/" + Coach.getInstance().getCoachId();

        // Use an HTTP client library like OkHttp to make the request
        new OkHttpClient().newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ScheduledCheck", "Error loading tasks from API", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : null;
                    try {
                        // 解析 JSON 字串
                        JSONObject jsonObject = new JSONObject(responseBody);
                        // 讀取分類名稱的值
                        String categoryName = jsonObject.getString("Value");
                        // 在 UI 執行緒中顯示分類名稱
                        runOnUiThread(() -> Toast.makeText(CoachHome.this, categoryName, Toast.LENGTH_SHORT).show());
                    } catch (JSONException e) {
                        Log.e("CoachHome", "Error parsing JSON response", e);
                    }
                } else {
                    Log.e("CoachHome", "Unsuccessful response from API");
                }
            }
        });
    }*/
}
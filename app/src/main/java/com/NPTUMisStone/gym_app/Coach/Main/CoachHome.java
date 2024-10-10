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

import com.NPTUMisStone.gym_app.Coach.Records.BookingList;
import com.NPTUMisStone.gym_app.Coach.Records.BookingDetail;
import com.NPTUMisStone.gym_app.Coach.Scheduled.ScheduledSet;
import com.NPTUMisStone.gym_app.Coach.Scheduled.ScheduledCheck;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.Advertisement;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.ContactInfo;
import com.NPTUMisStone.gym_app.User_And_Coach.ProgressBarHandler;

import java.sql.Connection;
import java.util.ArrayList;
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
        init_banner();
        //getString();
    }

    private void init_coachInfo() {
        ((TextView) findViewById(R.id.CoachHome_nameText)).setText(getString(R.string.Coach_welcome, Coach.getInstance().getCoachName()));
        ((TextView) findViewById(R.id.CoachHome_idText)).setText(getString(R.string.Coach_id, Coach.getInstance().getCoachId()));
        ImageView coach_image = findViewById(R.id.CoachHome_photoImage);
        coach_image.setOnClickListener(v -> startActivity(new Intent(this, CoachInfo.class)));
        registerReceiver(broadcastReceiver, new IntentFilter("com.NPTUMisStone.gym_app.LOGOUT"), Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Context.RECEIVER_NOT_EXPORTED : 0);
        byte[] image = Coach.getInstance().getCoachImage(); //將byte[]轉換成Bitmap：https://stackoverflow.com/questions/3520019/display-image-from-bytearray
        if (image != null)
            coach_image.setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
    }

    private void init_banner() {
        ViewPager2 user_viewPager = findViewById(R.id.CoachHome_viewPager);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.CoachHome_swipeRefreshLayout);
        Advertisement.init_banner(this, MyConnection, adList, user_viewPager, swipeRefreshLayout);
    }

    public void onClick(View view) {
        if (progressBarHandler.isLoading()) return;
        progressBarHandler.showProgressBar();
        int id = view.getId();
        try {
            if (id == R.id.CoachHome_addButton)
                startActivity(new Intent(this, ScheduledSet.class));
            else if (id == R.id.CoachHome_checkButton)
                startActivity(new Intent(this, ScheduledCheck.class));
            else if (id == R.id.CoachHome_decideButton)
                startActivity(new Intent(this, BookingDetail.class));
            else if (id == R.id.CoachHome_historyButton)
                startActivity(new Intent(this, BookingList.class));
            else if (id == R.id.CoachHome_workButton)
                startActivity(new Intent(this, Achievement.class));
            else if (id == R.id.CoachHome_contactButton)
                startActivity(new Intent(this, ContactInfo.class));
        } catch (Exception e) {
            Log.e("Button", "Button click error", e);
        }
    }

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
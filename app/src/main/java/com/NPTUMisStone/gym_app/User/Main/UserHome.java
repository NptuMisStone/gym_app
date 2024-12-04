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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Class.ClassList;
import com.NPTUMisStone.gym_app.User.Coach.CoachList;
import com.NPTUMisStone.gym_app.User.Like.UserLike;
import com.NPTUMisStone.gym_app.User.Records.Appointment;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.AdHelper;
import com.NPTUMisStone.gym_app.User_And_Coach.UI.Contact;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.Map.View;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ProgressBarHandler;

import java.sql.Connection;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class UserHome extends AppCompatActivity {
    Connection MyConnection;
    ProgressBarHandler progressBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_main_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initSQLConnection();
        progressBarHandler = new ProgressBarHandler(this, findViewById(android.R.id.content));
        init_userinfo();
        AdHelper.initializeAndLoadAd(this, R.id.UserHome_adView);
    }
    private void initSQLConnection() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try(Connection connection = new SQLConnection(findViewById(R.id.main)).IWantToConnection()){
                new Handler(Looper.getMainLooper()).post(() -> MyConnection = connection);
            } catch (Exception e) {
                Log.e("SQL", "Connection error", e);
            }
        });
    }
    private void init_userinfo() {
        ((TextView) findViewById(R.id.UserHome_nameText)).setText(getGreetingMessage());
        ((TextView) findViewById(R.id.UserHome_idText)).setText(getString(R.string.All_idText, User.getInstance().getUserId()));
        findViewById(R.id.UserHome_editButton).setOnClickListener(v -> startActivity(new Intent(this, UserInfo.class)));
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
            if (id == R.id.UserHome_classButton) startActivity(new Intent(this, ClassList.class));
            else if (id == R.id.UserHome_coachButton) startActivity(new Intent(this, CoachList.class));
            else if (id == R.id.UserHome_loveButton) startActivity(new Intent(this, UserLike.class));
            else if (id == R.id.UserHome_historyButton) startActivity(new Intent(this, Appointment.class));
            else if (id == R.id.UserHome_gymButton) startActivity(new Intent(this, View.class));
            else if (id == R.id.UserHome_contactButton) startActivity(new Intent(this, Contact.class));
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
        init_userinfo();
        Log.d("ActivityState", "Activity resumed, progress bar hidden.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
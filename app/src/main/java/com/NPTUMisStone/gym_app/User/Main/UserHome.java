package com.NPTUMisStone.gym_app.User.Main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Class.ClassDetail;
import com.NPTUMisStone.gym_app.User.Class.AllClass;
import com.NPTUMisStone.gym_app.User.AllCoach.AllCoach;
import com.NPTUMisStone.gym_app.User.Like.UserLike;
import com.NPTUMisStone.gym_app.User.Records.AppointmentAll;
import com.NPTUMisStone.gym_app.User_And_Coach.AdHelper;
import com.NPTUMisStone.gym_app.User_And_Coach.Contact;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.Map.Map_salon;
import com.NPTUMisStone.gym_app.User_And_Coach.ProgressBarHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class UserHome extends AppCompatActivity {
    Connection MyConnection;
    //List<Advertisement> adList = new ArrayList<>();
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
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        progressBarHandler = new ProgressBarHandler(this, findViewById(android.R.id.content));
        init_userinfo();
        //init_banner();
        AdHelper.initializeAndLoadAd(this, R.id.UserHome_adView);

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
        byte[] image = User.getInstance().getUserImage();
        if (image != null)
            ((ImageView)findViewById(R.id.UserHome_photoImage)).setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(image)));
    }

    /*private void init_banner() {
        ViewPager2 user_viewPager = findViewById(R.id.UserHome_viewPager);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.UserHome_swipeRefreshLayout);
        Advertisement.init_banner(this, MyConnection, adList, user_viewPager, swipeRefreshLayout);
    }*/
    //【《Android》『呼叫外部 App』- 透過 startActivity 執行外部 App 的基本方法】：
    // https://xnfood.com.tw/android-call-app-startactivity/
    //(可參考)：Android—组件化的搭建：https://www.cnblogs.com/wang66a/p/17769227.html
    public void onClick(View view) {
        if (progressBarHandler.isLoading()) return;
        progressBarHandler.showProgressBar();
        int id = view.getId();
        try {
            if (id == R.id.UserHome_classButton) startActivity(new Intent(this, AllClass.class));
            else if (id == R.id.UserHome_coachButton) startActivity(new Intent(this, AllCoach.class));
            else if (id == R.id.UserHome_loveButton) startActivity(new Intent(this, UserLike.class));
            else if (id == R.id.UserHome_historyButton) startActivity(new Intent(this, AppointmentAll.class));
            //else if (id == R.id.UserHome_gymButton) startNavigationActivity();
            else if (id == R.id.UserHome_gymButton) startActivity(new Intent(this, Map_salon.class));
            else if (id == R.id.UserHome_contactButton) startActivity(new Intent(this, Contact.class));
        } catch (Exception e) {
            Log.e("Button", "Button click error", e);
        }
    }
    private void startNavigationActivity() {
        // 查詢資料庫
        List<String[]> addresses = getAddressesFromDatabase();
        // 將資料轉換為陣列
        String[][] addressArray = new String[addresses.size()][2];
        for (int i = 0; i < addresses.size(); i++) addressArray[i] = addresses.get(i);
        // 創建 Intent 並傳遞資料
        Intent intent = new Intent();
        //intent.setComponent(new ComponentName("com.NptuMisStone.mapview", "com.NptuMisStone.mapview.ClassMap"));
        intent.setComponent(new ComponentName("com.example.mapboxnavigation", "com.example.mapboxnavigation.Navigation"));
        intent.putExtra("addresses", addressArray);
        createTask.launch(intent);
    }
    ActivityResultLauncher<Intent> createTask = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                int resultString = data.getIntExtra("看更多課程ID", -1);
                startActivity(new Intent(this, ClassDetail.class).putExtra("看更多課程ID", resultString));
            }
        }
        else Toast.makeText(this, "未選擇任何課程", Toast.LENGTH_SHORT).show();
    });
    private List<String[]> getAddressesFromDatabase() {
        List<String[]> addresses = new ArrayList<>();
        // 假設你有一個 SQLConnection 類來處理資料庫連接
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        String query = "SELECT 課程編號,縣市,行政區,顯示地點地址, 顯示地點名稱 FROM [健身教練課程-有排課的]";
        try (Statement statement = MyConnection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next())
                addresses.add(new String[]{ resultSet.getString("縣市")+resultSet.getString("行政區")+resultSet.getString("顯示地點地址"), resultSet.getString("顯示地點名稱"), resultSet.getString("課程編號") });
        } catch (SQLException e) {
            Log.e("Database", "Error querying database", e);
        }
        return addresses;
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
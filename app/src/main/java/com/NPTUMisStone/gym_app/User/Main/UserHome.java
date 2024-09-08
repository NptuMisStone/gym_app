package com.NPTUMisStone.gym_app.User.Main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Search.CoachLove;
import com.NPTUMisStone.gym_app.User.Records.AppointmentAll;
import com.NPTUMisStone.gym_app.User.Search.GymList;
import com.NPTUMisStone.gym_app.User.Search.Search;
import com.NPTUMisStone.gym_app.User_And_Coach.Advertisement;
import com.NPTUMisStone.gym_app.User_And_Coach.BannerViewAdapter;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.ContactInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class UserHome extends AppCompatActivity {
    Connection MyConnection;
    List<Advertisement> adList = new ArrayList<>();
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
        init_userinfo();
        init_banner();
    }
    private void init_userinfo() {
        TextView user = findViewById(R.id.UserHome_nameText);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hour < 6) user.setText(getString(R.string.User_welcome, "🌙 凌晨，該休息了", User.getInstance().getUserName()));
        else if(hour < 12) user.setText(getString(R.string.User_welcome, "☀️ 早上好", User.getInstance().getUserName()));
        else if(hour < 18) user.setText(getString(R.string.User_welcome, "🌤️ 下午好", User.getInstance().getUserName()));
        else user.setText(getString(R.string.User_welcome, "🌙 晚上好", User.getInstance().getUserName()));
        ImageView user_image = findViewById(R.id.UserHome_photoImage);
        user_image.setOnClickListener(v -> startActivity(new Intent(this, UserInfo.class)));
        byte[] image = User.getInstance().getUserImage();
        if (image != null) user_image.setImageBitmap(ImageHandle.getBitmap(image));
    }
    private void init_banner() {
        ViewPager2 user_viewPager = findViewById(R.id.UserHome_viewPager);  // Setup ViewPager2 and load advertisements
        user_viewPager.setAdapter(new BannerViewAdapter(adList, this));
        loadAdvertisements(user_viewPager);
        startAutoSlide(user_viewPager); // Start auto slide
        /*SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.UserHome_swipeRefreshLayout); // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadAdvertisements(user_viewPager);
            swipeRefreshLayout.setRefreshing(false);
        });*/
    }
    private void loadAdvertisements(ViewPager2 viewPager) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (MyConnection == null || MyConnection.isClosed())
                    MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                String searchQuery = "SELECT 圖片, 連結, 上架日, 下架日 FROM 廣告 WHERE GETDATE() BETWEEN 上架日 AND 下架日";
                ResultSet resultSet = MyConnection.prepareStatement(searchQuery).executeQuery();
                while (resultSet.next()) {
                    byte[] image = resultSet.getBytes("圖片");
                    String link = resultSet.getString("連結");
                    Date startDate = resultSet.getDate("上架日");
                    Date endDate = resultSet.getDate("下架日");
                    adList.add(new Advertisement(image, link, startDate, endDate));
                }
                Objects.requireNonNull(viewPager.getAdapter()).notifyItemRangeChanged(0, adList.size());
            } catch (Exception e) {
                Log.e("SQL", "Error in SQL", e);
            }
        });
    }

    private void startAutoSlide(ViewPager2 viewPager) {
        Handler sliderHandler = new Handler(Looper.getMainLooper());
        sliderHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem((viewPager.getCurrentItem() + 1) % adList.size());
                sliderHandler.postDelayed(this, 6000);
            }
        }, 6000);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.UserHome_coachButton) {
            startActivity(new Intent(this, Search.class));
        } else if (id == R.id.UserHome_loveButton) {
            startActivity(new Intent(this, CoachLove.class));
        } else if (id == R.id.UserHome_appointmentButton) {
            startActivity(new Intent(this, AppointmentAll.class));
        } else if (id == R.id.UserHome_gymButton) {
            startActivity(new Intent(this, GymList.class));
        } else if (id == R.id.UserHome_sportsButton) {
            //startActivity(new Intent(this, SportList.class));
        } else if (id == R.id.UserHome_contactButton) {
            startActivity(new Intent(this, ContactInfo.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        init_userinfo();
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("歡迎使用健身房預約系統");
        builder.setMessage("本系統提供健身房、教練、課程預約服務，歡迎使用。");
        builder.setPositiveButton("確定", null);
        builder.show();*/
    }
}
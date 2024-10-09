package com.NPTUMisStone.gym_app.User.Main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Search.CoachLove;
import com.NPTUMisStone.gym_app.User.Records.AppointmentAll;
import com.NPTUMisStone.gym_app.User.Search.GymList;
import com.NPTUMisStone.gym_app.User.Search.Search;
import com.NPTUMisStone.gym_app.User_And_Coach.Advertisement;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.ContactInfo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
        ViewPager2 user_viewPager = findViewById(R.id.UserHome_viewPager);
        Advertisement.init_banner(this, MyConnection, adList, user_viewPager, null);
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
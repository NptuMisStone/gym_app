package com.NPTUMisStone.gym_app.User.AllCoach.DetailCoach;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.AllClass.DetailClass.User_Detail_Class_Fragment;
import com.NPTUMisStone.gym_app.User.AllClass.DetailClass.User_Detail_Class_Time_Fragment;
import com.NPTUMisStone.gym_app.User.AllClass.User_All_Class;
import com.NPTUMisStone.gym_app.User.AllCoach.User_All_Coach;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.google.android.material.tabs.TabLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User_Coach_Detail extends AppCompatActivity {

    FrameLayout frameLayout;
    TabLayout tabLayout;
    Connection MyConnection;
    ImageButton likebtn;
    ImageView coachimg;
    Intent intent;
    int coachID;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_coach_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        intent = getIntent();
        coachID = intent.getIntExtra("看更多教練ID", 0);

        frameLayout=(FrameLayout)findViewById(R.id.DetailCoachFrameLayout);
        tabLayout=(TabLayout)findViewById(R.id.DetailCoachTabLayout);

        // 將 coachID 傳遞給 Fragment
        // 先設置 Fragment
        tabLayout.selectTab(tabLayout.getTabAt(1));
        User_Coach_Detail_Info_Fragment infoFragment = new User_Coach_Detail_Info_Fragment();
        Bundle bundle = new Bundle();
        bundle.putInt("coachID", coachID);
        infoFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.DetailCoachFrameLayout, infoFragment)
                .addToBackStack(null)
                .commit();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new User_Coach_Detail_Class_Fragment();
                        break;
                    case 1:
                        fragment = infoFragment;
                        break;
                    case 2:
                        fragment = new User_Coach_Detail_Comment_Fragment();
                        break;
                }
                if (fragment != null) {
                    // 傳遞 classID 给新的 Fragment
                    Bundle bundle = new Bundle();
                    bundle.putInt("coachID", coachID);
                    fragment.setArguments(bundle);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.DetailCoachFrameLayout, fragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                } else {
                    Log.e("User_Coach_Detail", "無法載入 Fragment");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        likebtn=findViewById(R.id.user_like_detail_coach_btn);
        coachimg=findViewById(R.id.user_detail_coach_img);
        try {
            String query = "SELECT 健身教練圖片 FROM [健身教練審核合併] WHERE 健身教練編號 = ?" ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,coachID);
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
                if (rs.getBytes("健身教練圖片") != null) {
                    Bitmap bitmap = ImageHandle.getBitmap(rs.getBytes("健身教練圖片"));
                    coachimg.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
                }
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            String sql = "SELECT COUNT(*) FROM 教練被收藏 WHERE 健身教練編號 = ? AND 使用者編號 = ?";
            PreparedStatement Statement = MyConnection.prepareStatement(sql);
            Statement.setInt(1,coachID);
            Statement.setInt(2, User.getInstance().getUserId());
            ResultSet rs = Statement.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    likebtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.like1));
                } else {
                    likebtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dislike2));
                }
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        likebtn.setOnClickListener(v -> {
            Drawable currentDrawable = likebtn.getDrawable();
            if (currentDrawable.getConstantState().equals(ContextCompat.getDrawable(this, R.drawable.dislike2).getConstantState())) {
                // 如果當前是 dislike 狀態，切換到 like
                likebtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.like1));

                // 更新資料庫
                try {
                    MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                    String insertSql = "INSERT INTO 教練被收藏 (使用者編號, 健身教練編號) VALUES (?, ?)";
                    PreparedStatement insertStatement = MyConnection.prepareStatement(insertSql);
                    insertStatement.setInt(1, User.getInstance().getUserId());
                    insertStatement.setInt(2, coachID);
                    insertStatement.executeUpdate();
                    insertStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                // 如果當前是 like 狀態，切換到 dislike
                likebtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dislike2));
                try {
                    MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                    String deleteSql = "DELETE FROM 教練被收藏 WHERE 健身教練編號 = ? AND 使用者編號 = ?";
                    PreparedStatement deleteStatement = MyConnection.prepareStatement(deleteSql);
                    deleteStatement.setInt(1, coachID);
                    deleteStatement.setInt(2, User.getInstance().getUserId());
                    deleteStatement.executeUpdate();
                    deleteStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        });
    }
    public  void user_detail_coach_goback(View view){
        Intent it =new Intent(this, User_All_Coach.class);
        startActivity(it);
        finish();
    }
}
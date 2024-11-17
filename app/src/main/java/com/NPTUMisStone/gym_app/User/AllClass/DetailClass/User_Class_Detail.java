package com.NPTUMisStone.gym_app.User.AllClass.DetailClass;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.google.android.material.tabs.TabLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class User_Class_Detail extends AppCompatActivity {
    Connection MyConnection;
    ImageButton likeButton;
    int classID;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_class_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        classID = getIntent().getIntExtra("看更多課程ID", 0);
        // 將 classID 傳遞給 Fragment
        // 先設置 Fragment
        User_Detail_Class_Fragment detailFragment = new User_Detail_Class_Fragment();
        Bundle bundle = new Bundle();
        bundle.putInt("classID", classID);
        detailFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().addToBackStack(null)
                .replace(R.id.DetailClassFrameLayout, detailFragment).commit();
        ((TabLayout)findViewById(R.id.DetailClassTabLayout)).addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = switch (tab.getPosition()) {
                    case 0 -> detailFragment;
                    case 1 -> new User_Detail_Class_Time_Fragment();
                    default -> null;
                };
                if (fragment != null) {
                    // 傳遞 classID 给新的 Fragment
                    Bundle bundle = new Bundle();
                    bundle.putInt("classID", classID);
                    fragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.DetailClassFrameLayout, fragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                } else {
                    Log.e("AppointmentAll", "無法載入 Fragment");
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        likeButton =findViewById(R.id.user_like_detail_class_btn);
        try {
            String query = "SELECT 課程圖片 FROM 健身教練課表課程合併 WHERE 課程編號 = ?" ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,classID);
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
                if (rs.getBytes("課程圖片") != null) {
                    Bitmap bitmap = ImageHandle.getBitmap(rs.getBytes("課程圖片"));
                    ((ImageView)findViewById(R.id.user_detail_class_img)).setImageBitmap(ImageHandle.resizeBitmap(bitmap));
                }
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            Log.e("User_Class_Detail", "SQL error");
        }
        try {
            MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
            String sql = "SELECT COUNT(*) FROM 課程被收藏 WHERE 課程編號 = ? AND 使用者編號 = ?";
            PreparedStatement Statement = MyConnection.prepareStatement(sql);
            Statement.setInt(1,classID);
            Statement.setInt(2, User.getInstance().getUserId());
            ResultSet rs = Statement.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0 ) likeButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.user_like_ic_love));
                else likeButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.user_like_ic_not_love));
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            Log.e("User_Class_Detail", "SQL error");
        }
        likeButton.setOnClickListener(v -> {
            Drawable currentDrawable = likeButton.getDrawable();
            if (Objects.equals(currentDrawable.getConstantState(), Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.user_like_ic_not_love)).getConstantState())) {
                // 如果當前是 dislike 狀態，切換到 like
                likeButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.user_like_ic_love));
                // 更新資料庫
                try {
                    MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                    String insertSql = "INSERT INTO 課程被收藏 (使用者編號, 課程編號) VALUES (?, ?)";
                    PreparedStatement insertStatement = MyConnection.prepareStatement(insertSql);
                    insertStatement.setInt(1, User.getInstance().getUserId());
                    insertStatement.setInt(2, classID);
                    insertStatement.executeUpdate();
                    insertStatement.close();
                } catch (SQLException e) {
                    Log.e("User_Class_Detail", "SQL error");
                }
            } else {
                // 如果當前是 like 狀態，切換到 dislike
                likeButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.user_like_ic_not_love));
                try {
                    MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                    String deleteSql = "DELETE FROM 課程被收藏 WHERE 課程編號 = ? AND 使用者編號 = ?";
                    PreparedStatement deleteStatement = MyConnection.prepareStatement(deleteSql);
                    deleteStatement.setInt(1, classID);
                    deleteStatement.setInt(2, User.getInstance().getUserId());
                    deleteStatement.executeUpdate();
                    deleteStatement.close();
                } catch (SQLException e) {
                    Log.e("User_Class_Detail", "SQL error");
                }
            }

        });
    }
}
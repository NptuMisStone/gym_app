package com.NPTUMisStone.gym_app.User.Class;

import android.content.Context;
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
import com.NPTUMisStone.gym_app.User.Class.Fragment.ClassDetail_InfoFragment;
import com.NPTUMisStone.gym_app.User.Class.Fragment.ClassDetail_TimeFragment;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.Map.Redirecting;
import com.google.android.material.tabs.TabLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ClassDetail extends AppCompatActivity {
    private Connection MyConnection;
    private ImageButton likeButton;
    private int classID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_class_detail);
        setupWindowInsets();
        initializeComponents();
        setupListeners();
    }

    private void initializeComponents() {
        classID = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE).getInt("看更多課程ID", 0);
        setupFragment();
        setupTabLayout();
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        likeButton = findViewById(R.id.ClassDetail_likeButton);
        loadClassImage();
        updateLikeButtonState();
        setupLikeButtonListener();
    }

    private void setupListeners() {
        findViewById(R.id.ClassDetail_backButton).setOnClickListener(v -> finish());
        findViewById(R.id.ClassDetail_directionButton).setOnClickListener(v -> new Redirecting(this, Redirecting.getLocationAddress(MyConnection,classID)).getCurrentLocation());
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupFragment() {
        ClassDetail_InfoFragment detailFragment = new ClassDetail_InfoFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.ClassDetail_detailFrame, detailFragment).commit();
    }

    private void setupTabLayout() {
        ((TabLayout) findViewById(R.id.ClassDetail_chooseTab)).addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = getFragmentForTab(tab.getPosition());
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.ClassDetail_detailFrame, fragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                } else {
                    Log.e("ClassDetail", "無法載入 Fragment");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private Fragment getFragmentForTab(int position) {
        return switch (position) {
            case 0 -> new ClassDetail_InfoFragment();
            case 1 -> new ClassDetail_TimeFragment();
            default -> null;
        };
    }

    private void loadClassImage() {
        executeQuery("SELECT 課程圖片 FROM 健身教練課程 WHERE 課程編號 = ?", rs -> {
            if (rs.next() && rs.getBytes("課程圖片") != null) {
                Bitmap bitmap = ImageHandle.getBitmap(rs.getBytes("課程圖片"));
                ((ImageView) findViewById(R.id.ClassDetail_classImage)).setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            } else {
                Log.e("User_Class_Detail", "No image found");
            }
        });
    }

    private void updateLikeButtonState() {
        executeQuery("SELECT COUNT(*) FROM 課程被收藏 WHERE 課程編號 = ? AND 使用者編號 = ?", rs -> {
            if (rs.next() && rs.getInt(1) > 0)
                likeButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.user_like_ic_love));
            else likeButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.user_like_ic_not_love));
        });
    }

    private void setupLikeButtonListener() {
        likeButton.setOnClickListener(v -> {
            Drawable currentDrawable = likeButton.getDrawable();
            if (Objects.equals(currentDrawable.getConstantState(), Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.user_like_ic_not_love)).getConstantState())) {
                likeButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.user_like_ic_love));
                updateDatabase("INSERT INTO 課程被收藏 (使用者編號, 課程編號) VALUES (?, ?)");
            } else {
                likeButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.user_like_ic_not_love));
                updateDatabase("DELETE FROM 課程被收藏 WHERE 課程編號 = ? AND 使用者編號 = ?");
            }
        });
    }

    private void executeQuery(String query, ResultSetHandler handler) {
        try (PreparedStatement statement = MyConnection.prepareStatement(query)) {
            statement.setInt(1, classID);
            if (query.contains("AND 使用者編號 = ?"))
                statement.setInt(2, User.getInstance().getUserId());
            try (ResultSet rs = statement.executeQuery()){
                handler.handle(rs);
            }
        } catch (SQLException e) {
            Log.e("User_Class_Detail", "SQL error", e);
        }
    }

    private void updateDatabase(String sql) {
        try (PreparedStatement statement = MyConnection.prepareStatement(sql)) {
            statement.setInt(1, User.getInstance().getUserId());
            statement.setInt(2, classID);
            statement.executeUpdate();
        } catch (SQLException e) {
            Log.e("User_Class_Detail", "SQL error", e);
        }
    }

    @FunctionalInterface
    private interface ResultSetHandler {
        void handle(ResultSet rs) throws SQLException;
    }
}
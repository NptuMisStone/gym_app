package com.NPTUMisStone.gym_app.User.Search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;

public class CoachList extends AppCompatActivity {
    Connection MyConnection;
    ArrayList<CoachListData> coachList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_search_coach_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ShimmerFrameLayout shimmerFrameLayout = findViewById(R.id.shimmerLayout);
        shimmerFrameLayout.startShimmer();
        fetchCoaches(shimmerFrameLayout);
        findViewById(R.id.findCoach_goBack).setOnClickListener(v -> finish());
    }

    private void fetchCoaches(ShimmerFrameLayout shimmerFrameLayout) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                coachList.clear();
                String sql = "SELECT 健身教練編號, 健身教練圖片, 健身教練姓名, 健身教練介紹 FROM 健身教練資料";
                ResultSet rs = MyConnection.createStatement().executeQuery(sql);
                while (rs.next())
                    coachList.add(new CoachListData(rs.getInt("健身教練編號"), rs.getBytes("健身教練圖片"), rs.getString("健身教練姓名"), rs.getString("健身教練介紹")));
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
            }
            new Handler(Looper.getMainLooper()).post(() -> updateUI(shimmerFrameLayout));
        });
    }

    private void updateUI(ShimmerFrameLayout shimmerFrameLayout) {
        if (!isFinishing()) {
            CoachListAdapter coachListAdapter = new CoachListAdapter(CoachList.this, coachList);
            RecyclerView coachRecyclerView = findViewById(R.id.coach_recyclerView);
            coachRecyclerView.setLayoutManager(new LinearLayoutManager(CoachList.this));
            coachRecyclerView.setAdapter(coachListAdapter);
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
        }
    }

}
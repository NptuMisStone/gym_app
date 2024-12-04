package com.NPTUMisStone.gym_app.Coach.Records.Fragment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Coach.Records.Adapter;
import com.NPTUMisStone.gym_app.Coach.Records.Data;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.databinding.CoachAppointmentPastBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;

public class PastFragment extends Fragment {

    private CoachAppointmentPastBinding binding;
    private Connection MyConnection;
    ArrayList<Data> appointmentData = new ArrayList<>();
    private ProgressBar progressBar;
    TextView nodata;

    public static PastFragment newInstance() {
        return new PastFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding= CoachAppointmentPastBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        nodata=binding.coachApNodata;
        nodata.setVisibility(View.GONE);
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        fetchPastAppointments();
        return root;
    }
    private void fetchPastAppointments() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                appointmentData.clear();
                String sql = "SELECT * FROM 健身教練課表課程合併 WHERE 健身教練編號= ? AND 日期 < ? ORDER BY 日期 DESC, 開始時間 ASC";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, Coach.getInstance().getCoachId());
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                searchStatement.setDate(2, today);
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next()) {
                    int locationType = rs.getInt("地點類型");
                    String city = rs.getString("縣市");
                    String district = rs.getString("行政區");
                    String locationName = rs.getString("地點名稱");

                    // 設置地點
                    String locationText;
                    if (locationType == 2) { // 到府服務
                        locationText = "到府 (" + city + district + ")";
                    } else { // 固定地點
                        locationText = locationName;
                    }

                    // 將資料添加到 appointmentData 列表
                    appointmentData.add(new Data(
                            rs.getInt("課表編號"),
                            rs.getDate("日期"),
                            rs.getString("星期幾"),
                            rs.getString("開始時間"),
                            rs.getString("結束時間"),
                            rs.getString("課程名稱"),
                            rs.getInt("預約人數"),
                            rs.getInt("上課人數"),
                            locationText,   // 使用組合後的 locationText
                            rs.getString("地點類型"),
                            city,
                            district
                    ));
                }
                rs.close();
                searchStatement.close();
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
            }
            new Handler(Looper.getMainLooper()).post(this::updateUI);
        });

    }
    private void updateUI() {
        if (getActivity() != null && isAdded()) {
            Adapter coachAppointmentAdapter = new Adapter(this,appointmentData);
            RecyclerView coachApPastRecycleview   = binding.coachApPastRecycleview;
            coachApPastRecycleview.setLayoutManager(new LinearLayoutManager(getActivity()));
            coachApPastRecycleview.setAdapter(coachAppointmentAdapter);
            progressBar.setVisibility(View.GONE);
            if (appointmentData.isEmpty()) {
                nodata.setVisibility(View.VISIBLE);
            } else {
                nodata.setVisibility(View.GONE);
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 避免内存泄漏
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null && data.getBooleanExtra("刷新數據", false)) {
                fetchPastAppointments();
            }
        }
    }

}
package com.NPTUMisStone.gym_app.Coach.Records.CoachTodayAppointment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
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
import com.NPTUMisStone.gym_app.Coach.Records.Coach_AppointmentData;
import com.NPTUMisStone.gym_app.Coach.Records.Coach_Appointment_Adapter;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User.Records.CoachCancelAppointment.CoachCancelAppointmentViewModel;
import com.NPTUMisStone.gym_app.User.Records.User_AppointmentData;
import com.NPTUMisStone.gym_app.User.Records.User_Appointment_Adapter;
import com.NPTUMisStone.gym_app.databinding.CoachTodayAppointmentFragmentBinding;
import com.NPTUMisStone.gym_app.databinding.UserFragmentCoachCancelAppointmentBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;

public class CoachTodayAppointment extends Fragment {

    private CoachTodayAppointmentFragmentBinding binding;
    private Connection MyConnection;
    ArrayList<Coach_AppointmentData> appointmentData = new ArrayList<>();
    private ProgressBar progressBar;
    TextView nodata;
    public static CoachTodayAppointment newInstance() {
        return new CoachTodayAppointment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding= CoachTodayAppointmentFragmentBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        nodata=binding.coachApNodata;
        nodata.setVisibility(View.GONE);
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        fetchTodayAppointments();
        return root;
    }
    private void fetchTodayAppointments() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                appointmentData.clear();
                String sql = "SELECT * FROM 健身教練課表課程合併 WHERE 健身教練編號= ? AND 日期 = ? ORDER BY 開始時間";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, Coach.getInstance().getCoachId());
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                searchStatement.setDate(2, today);
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    appointmentData.add(new Coach_AppointmentData(
                            rs.getInt("課表編號"),
                            rs.getDate("日期"),
                            rs.getString("星期幾"),
                            rs.getString("開始時間"),
                            rs.getString("結束時間"),
                            rs.getString("課程名稱"),
                            rs.getInt("預約人數"),
                            rs.getInt("上課人數"),
                            rs.getString("地點名稱"),
                            rs.getString("地點類型")
                    ));
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
            Coach_Appointment_Adapter coachAppointmentAdapter = new Coach_Appointment_Adapter(this,appointmentData);
            RecyclerView coachApTodayRecycleview   = binding.coachApTodayRecycleview;
            coachApTodayRecycleview.setLayoutManager(new LinearLayoutManager(getActivity()));
            coachApTodayRecycleview.setAdapter(coachAppointmentAdapter);
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
                fetchTodayAppointments();
            }
        }
    }
}
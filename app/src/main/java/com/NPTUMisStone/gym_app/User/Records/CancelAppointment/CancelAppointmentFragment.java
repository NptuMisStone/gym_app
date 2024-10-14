package com.NPTUMisStone.gym_app.User.Records.CancelAppointment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.User.Records.User_AppointmentData;
import com.NPTUMisStone.gym_app.User.Records.User_Appointment_Adapter;
import com.NPTUMisStone.gym_app.databinding.UserFragmentCancelAppointmentBinding;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class CancelAppointmentFragment extends Fragment {

    private UserFragmentCancelAppointmentBinding binding;
    private User_Appointment_Adapter adapter;
    private final ArrayList<User_AppointmentData> appointmentData = new ArrayList<>();

    public static CancelAppointmentFragment newInstance() {
        return new CancelAppointmentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i("CancelAppointment", "CancelAppointmentFragment 已創建");
        binding = UserFragmentCancelAppointmentBinding.inflate(inflater, container, false);
        setupRecyclerView();
        fetchAppointments();
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new User_Appointment_Adapter(getContext(), appointmentData);
        binding.userApCancelRecycleview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.userApCancelRecycleview.setAdapter(adapter);
    }

    private void fetchAppointments() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (Connection conn = new SQLConnection(binding.getRoot()).IWantToConnection();
                 ResultSet rs = conn.createStatement().executeQuery(
                         "SELECT 預約編號,日期,星期幾,開始時間,課程時間長度,課程名稱,課程費用,健身教練圖片,健身教練姓名,預約狀態,備註 FROM [使用者預約-有預約的] WHERE [預約狀態] = 3")) {

                appointmentData.clear(); // 確保每次刷新都從空的列表開始
                boolean hasData = false;  // 新增標記檢查是否有數據

                while (rs.next()) {
                    hasData = true;  // 若有資料則設為 true
                    appointmentData.add(new User_AppointmentData(
                            rs.getInt("預約編號"),
                            rs.getDate("日期"),
                            rs.getString("星期幾"),
                            rs.getTime("開始時間"),
                            rs.getInt("課程時間長度"),
                            rs.getString("課程名稱"),
                            rs.getString("課程費用"),
                            rs.getBytes("健身教練圖片"),
                            rs.getString("健身教練姓名"),
                            rs.getInt("預約狀態"),
                            rs.getString("備註")
                    ));
                }

                if (!hasData) {
                    Log.e("CancelAppointment", "沒有任何預約數據！");
                }

                // 在主线程更新 UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    adapter.notifyDataSetChanged();  // 刷新 RecyclerView
                    Log.i("CancelAppointment", "已更新 UI 並顯示資料");
                });

            } catch (SQLException e) {
                Log.e("SQL", "Database error: " + e.getMessage());
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 避免内存泄漏
    }
}




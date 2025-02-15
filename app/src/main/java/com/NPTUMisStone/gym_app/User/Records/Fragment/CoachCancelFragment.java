package com.NPTUMisStone.gym_app.User.Records.Fragment;

import androidx.lifecycle.ViewModelProvider;

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

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User.Records.Adapter;
import com.NPTUMisStone.gym_app.User.Records.Data;
import com.NPTUMisStone.gym_app.databinding.UserAppointmentCoachCancelBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;

public class CoachCancelFragment extends Fragment {

    private UserAppointmentCoachCancelBinding binding;
    private Connection MyConnection;
    ArrayList<Data> appointmentData = new ArrayList<>();
    private ProgressBar progressBar;
    TextView nodata;
    public static CoachCancelFragment newInstance() {
        return new CoachCancelFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        CoachCancelViewModel appointmentCoachCancelViewModel =new ViewModelProvider(this).get(CoachCancelViewModel.class);
        binding=UserAppointmentCoachCancelBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        nodata=binding.userApNodata;
        nodata.setVisibility(View.GONE);
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        fetchAppointments();
        return root;
    }
    private void fetchAppointments() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                appointmentData.clear();
                String sql = "SELECT 預約編號,日期,星期幾,開始時間,課程時間長度,課程名稱,課程費用,健身教練圖片,健身教練姓名,預約狀態,備註 FROM [使用者預約-有預約的] WHERE [預約狀態] = 5 AND 使用者編號 = ?";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, User.getInstance().getUserId());
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    appointmentData.add(new Data(
                            rs.getInt("預約編號"),
                            rs.getDate("日期"),
                            rs.getString("星期幾"),
                            rs.getString("開始時間"),
                            rs.getInt("課程時間長度"),
                            rs.getString("課程名稱"),
                            rs.getString("課程費用"),
                            rs.getBytes("健身教練圖片"),
                            rs.getString("健身教練姓名"),
                            rs.getInt("預約狀態"),
                            rs.getString("備註")
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
            Adapter userAppointmentAdapter = new Adapter(getActivity(),appointmentData);
            RecyclerView coachcancelApRecyclerView = binding.userApCoachcancelRecycleview;
            coachcancelApRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            coachcancelApRecyclerView.setAdapter(userAppointmentAdapter);
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

}
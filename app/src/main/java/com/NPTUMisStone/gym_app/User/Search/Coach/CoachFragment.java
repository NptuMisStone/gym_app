package com.NPTUMisStone.gym_app.User.Search.Coach;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.databinding.UserSearchFragmentCoachBinding;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;

public class CoachFragment extends Fragment {

    private UserSearchFragmentCoachBinding binding;
    private Connection MyConnection;
    ArrayList<CoachListData> coachList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CoachViewModel coachViewModel = new ViewModelProvider(this).get(CoachViewModel.class);

        binding = UserSearchFragmentCoachBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ShimmerFrameLayout shimmerFrameLayout = binding.shimmerLayout;
        shimmerFrameLayout.startShimmer();
        fetchCoaches(shimmerFrameLayout);

        binding.findCoachGoBack.setOnClickListener(v -> requireActivity().finish());

        return root;
    }

    private void fetchCoaches(ShimmerFrameLayout shimmerFrameLayout) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
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
        if (getActivity() != null && isAdded()) {
            CoachListAdapter coachListAdapter = new CoachListAdapter(getActivity(), coachList);
            RecyclerView coachRecyclerView = binding.coachRecyclerView;
            coachRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            coachRecyclerView.setAdapter(coachListAdapter);
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
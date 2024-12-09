package com.NPTUMisStone.gym_app.User.Like.Fragment;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Class.ClassDetail;
import com.NPTUMisStone.gym_app.User.Coach.CoachDetail;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;
import com.NPTUMisStone.gym_app.databinding.UserLikeCoachFragmentBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class UserLike_CoachFragment extends Fragment {

    private UserLikeCoachFragmentBinding binding;
    private Connection MyConnection;
    ArrayList<UserLike_CoachFragment.User_Like_Coach_Data> like_coach_data =new ArrayList<>();
    private ProgressBar progressBar;
    TextView nodata;

    public static UserLike_CoachFragment newInstance() {
        return new UserLike_CoachFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        UserLike_CoachViewModel userLikeCoachViewModel =new ViewModelProvider(this).get(UserLike_CoachViewModel.class);
        binding=UserLikeCoachFragmentBinding.inflate(inflater,container,false);
        View root = binding.getRoot();

        nodata=binding.userLikeNodata;
        nodata.setVisibility(View.GONE);
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        fetchLikeCoach();
        return root;
    }
    private void fetchLikeCoach() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                like_coach_data.clear();
                String sql = "SELECT 健身教練審核合併.健身教練圖片, " +
                        "健身教練審核合併.健身教練姓名, " +
                        "健身教練審核合併.註冊類型, " +
                        "健身教練審核合併.健身教練編號 " +
                        "FROM 教練被收藏 " +
                        "JOIN 健身教練審核合併 ON 教練被收藏.健身教練編號 = 健身教練審核合併.健身教練編號 " +
                        "WHERE 教練被收藏.使用者編號 = ?";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, User.getInstance().getUserId());
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    like_coach_data.add(new UserLike_CoachFragment.User_Like_Coach_Data(
                            rs.getInt("健身教練編號"),
                            rs.getBytes("健身教練圖片"),
                            rs.getString("健身教練姓名"),
                            rs.getString("註冊類型")
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
            UserLike_CoachFragment.LikeCoachAdapter likeCoachAdapter = new UserLike_CoachFragment.LikeCoachAdapter(getActivity(),like_coach_data,binding.getRoot());
            RecyclerView likecoachRecyclerView = binding.userLikeCoachRecycleview;
            likecoachRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            likecoachRecyclerView.setAdapter(likeCoachAdapter);
            progressBar.setVisibility(View.GONE);
            if (like_coach_data.isEmpty()) {
                nodata.setVisibility(View.VISIBLE);
            } else {
                nodata.setVisibility(View.GONE);
            }

        }
    }
    static class User_Like_Coach_Data {
        int like_coachID;
        byte[] coachimage;
        String coachName,coachType;
        static ArrayList<User_Like_Coach_Data> likeCoachData = new ArrayList<>();

        public User_Like_Coach_Data(int like_coachID,  byte[] coachimage, String coachName, String coachType) {
            this.like_coachID=like_coachID;
            this.coachimage = coachimage;
            this.coachName = coachName;
            this.coachType=coachType;

        }
        public static ArrayList<User_Like_Coach_Data> getLikeCoachData() {
            if (likeCoachData == null) {
                return null;
            }
            return likeCoachData;
        }

        public int getLike_coachID() {
            return like_coachID;
        }

        public byte[] getCoachimage() {
            return coachimage;
        }

        public String getCoachName() {
            return coachName;
        }


        public String getCoachType(){return coachType;}

    }
    class LikeCoachAdapter extends RecyclerView.Adapter<UserLike_CoachFragment.LikeCoachAdapter.ViewHolder>
    {

        List<UserLike_CoachFragment.User_Like_Coach_Data> like_coach_dataList;
        Context context;
        View view;
        public LikeCoachAdapter(Context context, List<UserLike_CoachFragment.User_Like_Coach_Data> like_coach_dataList , View view) {
            this.context = context;
            this.like_coach_dataList = like_coach_dataList;
            this.view=view;
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView coach_image;
            TextView coach_name, coach_type;
            ImageButton more_class_btn,like_coach_btn;
            public ViewHolder(View itemView) {
                super(itemView);
                coach_image = itemView.findViewById(R.id.user_like_coach_img);
                coach_name = itemView.findViewById(R.id.user_like_class_people);
                coach_type=itemView.findViewById(R.id.user_like_coach_type);
                more_class_btn=itemView.findViewById(R.id.user_like_coach_info);
                like_coach_btn=itemView.findViewById(R.id.user_like_coach_btn);
            }
        }

        @NonNull
        @Override
        public UserLike_CoachFragment.LikeCoachAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_like_coach_item, parent, false);
            return new UserLike_CoachFragment.LikeCoachAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserLike_CoachFragment.LikeCoachAdapter.ViewHolder holder, int position) {
            UserLike_CoachFragment.User_Like_Coach_Data item = like_coach_dataList.get(position);

            if (item.getCoachimage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getCoachimage());
                holder.coach_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            } else {
                holder.coach_image.setImageResource(R.drawable.coach_main_ic_default);
            }
            holder.coach_name.setText(item.getCoachName());
            holder.coach_type.setText(item.getCoachType());
            holder.like_coach_btn.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("確認取消")
                        .setMessage("確定要取消此收藏嗎？")
                        .setPositiveButton("確認", (dialog, which) -> {
                            // 調用刪除方法
                            deleteLike(item.getLike_coachID(), position);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
            holder.more_class_btn.setOnClickListener(v -> {
                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("看更多教練ID", item.getLike_coachID());
                editor.apply(); // 保存
                Intent intent = new Intent(context, CoachDetail.class);
                startActivity(intent);
            });
        }
        private void deleteLike(int Like_coachID, int position) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                    String deleteSQL = "DELETE FROM 教練被收藏 WHERE 健身教練編號 = ? AND 使用者編號 = ?";
                    PreparedStatement deleteStatement = MyConnection.prepareStatement(deleteSQL);
                    deleteStatement.setInt(1, Like_coachID);
                    deleteStatement.setInt(2, User.getInstance().getUserId());
                    int rowsAffected = deleteStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        // 刪除成功後更新列表
                        like_coach_dataList.remove(position);
                        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "已取消收藏", Toast.LENGTH_SHORT).show());
                        fetchLikeCoach();
                    }
                    deleteStatement.close();
                } catch (SQLException e) {
                    Log.e("SQL", Objects.requireNonNull(e.getMessage()));
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "取消失敗", Toast.LENGTH_SHORT).show());
                }
            });
        }
        @Override
        public int getItemCount() {
            return like_coach_dataList.size();
        }
    }

}
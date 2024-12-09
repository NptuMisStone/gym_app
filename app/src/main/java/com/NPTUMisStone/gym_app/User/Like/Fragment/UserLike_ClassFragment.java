package com.NPTUMisStone.gym_app.User.Like.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;
import com.NPTUMisStone.gym_app.databinding.UserLikeClassFragmentBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class UserLike_ClassFragment extends Fragment {

    private UserLikeClassFragmentBinding binding;
    private Connection MyConnection;
    ArrayList<UserLike_ClassFragment.User_Like_Class_Data> like_class_data = new ArrayList<>();
    private ProgressBar progressBar;
    TextView nodata;

    public static UserLike_ClassFragment newInstance() {
        return new UserLike_ClassFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        UserLike_ClassViewModel userLikeClassViewModel = new ViewModelProvider(this).get(UserLike_ClassViewModel.class);
        binding = UserLikeClassFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        nodata = binding.userLikeNodata;
        nodata.setVisibility(View.GONE);
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        fetchLikeClass();
        return root;
    }

    private void fetchLikeClass() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                like_class_data.clear();
                String sql = "SELECT 課程編號, 課程圖片, 健身教練圖片, 課程名稱, 課程費用, 健身教練姓名, 地點類型 FROM 收藏課程 WHERE 使用者編號= ?";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, User.getInstance().getUserId());
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next()) {
                    like_class_data.add(new User_Like_Class_Data(
                            rs.getInt("課程編號"),
                            rs.getBytes("課程圖片"),
                            rs.getBytes("健身教練圖片"),
                            rs.getString("課程名稱"),
                            rs.getString("課程費用"),
                            rs.getString("健身教練姓名"),
                            rs.getInt("地點類型")
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
            LikeClassAdapter likeClassAdapter = new LikeClassAdapter(getActivity(), like_class_data, binding.getRoot());
            RecyclerView likeclassRecyclerView = binding.userLikeClassRecycleview;
            likeclassRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            likeclassRecyclerView.setAdapter(likeClassAdapter);
            progressBar.setVisibility(View.GONE);
            if (like_class_data.isEmpty()) {
                nodata.setVisibility(View.VISIBLE);
            } else {
                nodata.setVisibility(View.GONE);
            }
        }
    }

    static class User_Like_Class_Data {
        int classID, locationType;
        byte[] classImage, coachImage;
        String className, classPrice, coachName;

        public User_Like_Class_Data(int classID, byte[] classImage, byte[] coachImage, String className, String classPrice, String coachName, int locationType) {
            this.classID = classID;
            this.classImage = classImage;
            this.coachImage = coachImage;
            this.className = className;
            this.classPrice = classPrice;
            this.coachName = coachName;
            this.locationType = locationType;
        }
        public int getClassID() {
            return classID;
        }
        public byte[] getClassImage() {
            return classImage;
        }

        public byte[] getCoachImage() {
            return coachImage;
        }

        public String getClassName() {
            return className;
        }

        public String getClassPrice() {
            return classPrice;
        }

        public String getCoachName() {
            return coachName;
        }

        public int getLocationType() {
            return locationType;
        }
    }


    class LikeClassAdapter extends RecyclerView.Adapter<LikeClassAdapter.ViewHolder> {

        List<User_Like_Class_Data> likeClassDataList;
        Context context;
        View view;

        public LikeClassAdapter(Context context, List<User_Like_Class_Data> likeClassDataList, View view) {
            this.context = context;
            this.likeClassDataList = likeClassDataList;
            this.view = view;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView classImage,coachImage;
            TextView classPeopleSign, className, classPrice, coachName;
            ImageButton moreClassBtn, likeClassBtn;

            public ViewHolder(View itemView) {
                super(itemView);
                classImage = itemView.findViewById(R.id.user_like_class_img);
                coachImage = itemView.findViewById(R.id. user_detail_coach_img);
                classPeopleSign = itemView.findViewById(R.id.user_like_class_people_sign);
                className = itemView.findViewById(R.id.user_like_class_classname);
                classPrice = itemView.findViewById(R.id.user_like_class_price);
                coachName = itemView.findViewById(R.id.user_like_class_coachname);
                moreClassBtn = itemView.findViewById(R.id.user_like_class_info);
                likeClassBtn = itemView.findViewById(R.id.user_like_class_btn);
            }
        }

        @NonNull
        @Override
        public LikeClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_like_class_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User_Like_Class_Data item = likeClassDataList.get(position);

            if (item.getClassImage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getClassImage());
                holder.classImage.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            } else {
                holder.classImage.setImageResource(R.drawable.coach_class_main_ic_default);
            }
            if (item.getCoachImage() != null) {
                Bitmap coachBitmap = ImageHandle.getBitmap(item.getCoachImage());
                holder.coachImage.setImageBitmap(ImageHandle.resizeBitmap(coachBitmap));
            } else {
                holder.coachImage.setImageResource(R.drawable.coach_main_ic_default);
            }
            holder.className.setText(item.getClassName());
            holder.classPrice.setText("$" + item.getClassPrice().split("\\.")[0] + "/堂");
            holder.coachName.setText(item.getCoachName());

            if (item.getLocationType() == 2) { // 到府服務
                holder.classPeopleSign.setText("到府課程");
                holder.classPeopleSign.setBackgroundResource(R.drawable.class_type_label_bg); // 藍底
            } else { // 團體課程
                holder.classPeopleSign.setText("團體課程");
                holder.classPeopleSign.setBackgroundResource(R.drawable.class_type_label_red_bg); // 紅底
            }

            holder.likeClassBtn.setOnClickListener(v -> new AlertDialog.Builder(context)
                    .setTitle("確認取消")
                    .setMessage("確定要取消此收藏嗎？")
                    .setPositiveButton("確認", (dialog, which) -> deleteLike(item.getClassID(), position))
                    .setNegativeButton("取消", null)
                    .show());

            holder.moreClassBtn.setOnClickListener(v -> {
                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("看更多課程ID", item.getClassID());
                editor.apply();
                Intent intent = new Intent(context, ClassDetail.class);
                startActivity(intent);
            });
        }

        private void deleteLike(int classID, int position) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                    String deleteSQL = "DELETE FROM 課程被收藏 WHERE 課程編號 = ? AND 使用者編號 = ?";
                    PreparedStatement deleteStatement = MyConnection.prepareStatement(deleteSQL);
                    deleteStatement.setInt(1, classID);
                    deleteStatement.setInt(2, User.getInstance().getUserId());
                    int rowsAffected = deleteStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        likeClassDataList.remove(position);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            notifyDataSetChanged();
                            Toast.makeText(context, "已取消收藏", Toast.LENGTH_SHORT).show();
                        });
                        fetchLikeClass();
                    }
                    deleteStatement.close();
                } catch (SQLException e) {
                    Log.e("SQL", Objects.requireNonNull(e.getMessage()));
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "取消失敗", Toast.LENGTH_SHORT).show());
                }
            });
        }

        @Override
        public int getItemCount() {
            return likeClassDataList.size();
        }
    }
}

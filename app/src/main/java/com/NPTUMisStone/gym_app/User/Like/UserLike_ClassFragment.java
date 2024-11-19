package com.NPTUMisStone.gym_app.User.Like;

import android.app.AlertDialog;
import android.content.Context;
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
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
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
    ArrayList<UserLike_ClassFragment.User_Like_Class_Data> like_class_data =new ArrayList<>();
    private ProgressBar progressBar;
    TextView nodata;

    public static UserLike_ClassFragment newInstance() {
        return new UserLike_ClassFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        UserLike_ClassViewModel userLikeClassViewModel =new ViewModelProvider(this).get(UserLike_ClassViewModel.class);
        binding=UserLikeClassFragmentBinding.inflate(inflater,container,false);
        View root = binding.getRoot();

        nodata=binding.userLikeNodata;
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
                String sql = "SELECT * FROM 收藏課程 WHERE 使用者編號= ?";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, User.getInstance().getUserId());
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    like_class_data.add(new UserLike_ClassFragment.User_Like_Class_Data(
                            rs.getInt("課程編號"),
                            rs.getBytes("課程圖片"),
                            rs.getString("課程名稱"),
                            rs.getString("課程費用"),
                            rs.getString("健身教練姓名"),
                            rs.getString("課程內容介紹"),
                            rs.getString("上課人數")
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
            UserLike_ClassFragment.LikeClassAdapter likeClassAdapter = new UserLike_ClassFragment.LikeClassAdapter(getActivity(),like_class_data,binding.getRoot());
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
        int classID;
        byte[] classimage;
        String className,classPrice,coachName,classIntro,classPeople;

        static ArrayList<UserLike_ClassFragment.User_Like_Class_Data> likeClassData = new ArrayList<>();

        public User_Like_Class_Data(int classID,byte[] classimage,String className,String classPrice,String coachName,String classIntro,String classPeople) {
            this.classID=classID;
            this.classimage=classimage;
            this.className=className;
            this.classPrice=classPrice;
            this.coachName = coachName;
            this.classIntro=classIntro;
            this.classPeople=classPeople;
        }
        public static ArrayList<UserLike_ClassFragment.User_Like_Class_Data> getLikeClassData() {
            if (likeClassData == null) {
                return null;
            }
            return likeClassData;
        }


        private int getClassID() {
            return classID;
        }

        private byte[] getClassimage() {
            return classimage;
        }
        private String getClassName(){return className;}
        private String getClassPrice(){return  classPrice;}

        private String getCoachName() {
            return coachName;
        }

        private String getClassIntro() {
            return classIntro;
        }

        private String getClassPeople() {
            return classPeople;
        }
    }

    class LikeClassAdapter extends RecyclerView.Adapter<UserLike_ClassFragment.LikeClassAdapter.ViewHolder>
    {

        List<UserLike_ClassFragment.User_Like_Class_Data> like_class_dataList;
        Context context;
        View view;
        public LikeClassAdapter(Context context, List<UserLike_ClassFragment.User_Like_Class_Data> like_class_dataList , View view) {
            this.context = context;
            this.like_class_dataList = like_class_dataList;
            this.view=view;
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView class_image;
            TextView class_people_sign,class_name,class_price,coach_name,class_intro,class_people;
            ImageButton like_class_btn;
            public ViewHolder(View itemView) {
                super(itemView);
                class_image=itemView.findViewById(R.id.user_like_class_img);
                class_people_sign=itemView.findViewById(R.id.user_like_class_people_sign);
                class_name=itemView.findViewById(R.id.user_like_class_classname);
                class_price=itemView.findViewById(R.id.user_like_class_price);
                coach_name=itemView.findViewById(R.id.user_like_class_coachname);
                class_intro=itemView.findViewById(R.id.user_like_class_intro);
                class_people=itemView.findViewById(R.id.user_like_class_people);

                like_class_btn=itemView.findViewById(R.id.user_like_class_btn);
            }
        }

        @NonNull
        @Override
        public UserLike_ClassFragment.LikeClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_like_class_item, parent, false);
            return new UserLike_ClassFragment.LikeClassAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserLike_ClassFragment.LikeClassAdapter.ViewHolder holder, int position) {
            UserLike_ClassFragment.User_Like_Class_Data item = like_class_dataList.get(position);

            if (item.getClassimage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getClassimage());
                holder.class_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            }
            holder.class_name.setText(item.getClassName());
            holder.class_price.setText("$"+item.getClassPrice().split("\\.")[0]+"/堂");
            holder.coach_name.setText(item.getCoachName());
            holder.class_intro.setText(item.getClassIntro());
            holder.class_people.setText("人數："+item.getClassPeople()+"人");
            holder.like_class_btn.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("確認取消")
                        .setMessage("確定要取消此收藏嗎？")
                        .setPositiveButton("確認", (dialog, which) -> {
                            // 調用刪除方法
                            deleteLike(item.getClassID(), position);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });

            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                String SQL = "SELECT 上課人數 FROM 收藏課程 WHERE 課程編號 = ? ";
                PreparedStatement Statement = MyConnection.prepareStatement(SQL);
                Statement.setInt(1,item.getClassID());
                ResultSet rs = Statement.executeQuery();
                while (rs.next()) {
                    if(item.getClassPeople().equals("1")){
                        holder.class_people_sign.setText("一對一");
                    }
                    else {
                        holder.class_people_sign.setText("團體");
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

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
                        // 刪除成功後更新列表
                        like_class_dataList.remove(position);
                        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "已取消收藏", Toast.LENGTH_SHORT).show());
                        fetchLikeClass();
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
            return like_class_dataList.size();
        }
    }
}
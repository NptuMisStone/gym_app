package com.NPTUMisStone.gym_app.User.Coach;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class CoachList extends AppCompatActivity {

    Connection MyConnection;
    private ProgressBar progressBar;
    ArrayList<CoachList.User_All_Coach_Data> coach_data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_coach_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        progressBar = findViewById(R.id.progressBar_allcoach);
        progressBar.setVisibility(View.VISIBLE);
        fetchCoach();
    }
    private void fetchCoach() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                coach_data.clear();
                String sql = "SELECT 健身教練圖片,健身教練姓名,註冊類型,健身教練編號 FROM [健身教練審核合併] where 審核狀態 = 1 ";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    coach_data.add(new CoachList.User_All_Coach_Data(
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

        CoachList.AllCoachAdapter coachAdapter = new CoachList.AllCoachAdapter(this,coach_data,findViewById(R.id.main));
        RecyclerView coachRecyclerView = findViewById(R.id.userCoachRecycleview);
        coachRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        coachRecyclerView.setAdapter(coachAdapter);
        progressBar.setVisibility(View.GONE);
    }
    static class User_All_Coach_Data {
        int all_coachID;
        byte[] coachimage;
        String coachName,coachType;
        static ArrayList<CoachList.User_All_Coach_Data> allCoachData = new ArrayList<>();

        public User_All_Coach_Data(int all_coachID,  byte[] coachimage, String coachName, String coachType) {
            this.all_coachID=all_coachID;
            this.coachimage = coachimage;
            this.coachName = coachName;
            this.coachType=coachType;

        }
        public static ArrayList<CoachList.User_All_Coach_Data> getAllCoachData() {
            if (allCoachData == null) {
                return null;
            }
            return allCoachData;
        }

        private int getAll_coachID(){return all_coachID;}

        private byte[] getCoachimage(){return coachimage;}

        private String getCoachName(){return coachName;}


        private String getCoachType(){return coachType;}

    }
    class AllCoachAdapter extends RecyclerView.Adapter<CoachList.AllCoachAdapter.ViewHolder>
    {

        List<CoachList.User_All_Coach_Data> all_coach_dataList;
        Context context;
        View view;
        public AllCoachAdapter(Context context, List<CoachList.User_All_Coach_Data> all_coach_dataList , View view) {
            this.context = context;
            this.all_coach_dataList = all_coach_dataList;
            this.view=view;
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView coach_image;
            TextView coach_name, coach_type;
            ImageButton like_coach_btn,moreinfo_btn;
            public ViewHolder(View itemView) {
                super(itemView);
                coach_image = itemView.findViewById(R.id.user_all_coach_img);
                coach_name = itemView.findViewById(R.id.user_all_coach_people);
                coach_type=itemView.findViewById(R.id.user_all_coach_type);
                like_coach_btn=itemView.findViewById(R.id.user_all_coach_btn);
                moreinfo_btn=itemView.findViewById(R.id.user_all_coach_info);

            }
        }

        @NonNull
        @Override
        public CoachList.AllCoachAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_coach_list_item, parent, false);
            return new CoachList.AllCoachAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CoachList.AllCoachAdapter.ViewHolder holder, int position) {
            CoachList.User_All_Coach_Data item = all_coach_dataList.get(position);

            if (item.getCoachimage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getCoachimage());
                holder.coach_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            }
            holder.coach_name.setText(item.getCoachName());
            holder.coach_type.setText(item.getCoachType());
            holder.moreinfo_btn.setOnClickListener(v -> {
                SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("看更多教練ID", item.getAll_coachID());
                editor.apply(); // 保存
                Intent intent = new Intent(context, CoachDetail.class);
                startActivity(intent);
            });
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                String sql = "SELECT COUNT(*) FROM 教練被收藏 WHERE 健身教練編號 = ? AND 使用者編號 = ?";
                PreparedStatement Statement = MyConnection.prepareStatement(sql);
                Statement.setInt(1,item.getAll_coachID());
                Statement.setInt(2, User.getInstance().getUserId());
                ResultSet rs = Statement.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count > 0) {
                        holder.like_coach_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.user_like_ic_love));
                    } else {
                        holder.like_coach_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.user_like_ic_not_love));
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            holder.like_coach_btn.setOnClickListener(v -> {
                Drawable currentDrawable = holder.like_coach_btn.getDrawable();
                if (currentDrawable.getConstantState().equals(ContextCompat.getDrawable(context, R.drawable.user_like_ic_not_love).getConstantState())) {
                    // 如果當前是 dislike 狀態，切換到 like
                    holder.like_coach_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.user_like_ic_love));

                    // 更新資料庫
                    try {
                        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                        String insertSql = "INSERT INTO 教練被收藏 (使用者編號, 健身教練編號) VALUES (?, ?)";
                        PreparedStatement insertStatement = MyConnection.prepareStatement(insertSql);
                        insertStatement.setInt(1, User.getInstance().getUserId());
                        insertStatement.setInt(2, item.getAll_coachID());
                        insertStatement.executeUpdate();
                        insertStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 如果當前是 like 狀態，切換到 dislike
                    holder.like_coach_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.user_like_ic_not_love));
                    try {
                        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                        String deleteSql = "DELETE FROM 教練被收藏 WHERE 健身教練編號 = ? AND 使用者編號 = ?";
                        PreparedStatement deleteStatement = MyConnection.prepareStatement(deleteSql);
                        deleteStatement.setInt(1, item.getAll_coachID());
                        deleteStatement.setInt(2, User.getInstance().getUserId());
                        deleteStatement.executeUpdate();
                        deleteStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            });

        }


        @Override
        public int getItemCount() {
            return all_coach_dataList.size();
        }
    }
    public void  user_All_Coach_goback(View view){
        finish();
    }
}
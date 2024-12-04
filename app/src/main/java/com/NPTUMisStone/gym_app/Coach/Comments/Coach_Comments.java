package com.NPTUMisStone.gym_app.Coach.Comments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Coach_Comments extends AppCompatActivity {
    Connection MyConnection;
    ArrayList<Coach_Comments_Data> coach_comments_data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_comments);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        // 返回按鈕
        findViewById(R.id.coach_comments_back).setOnClickListener(v -> finish());
        fetchComments();
    }
    private void fetchComments() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                coach_comments_data.clear();
                String sql = "SELECT * FROM [查看評論] WHERE [健身教練編號] = ?";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, Coach.getInstance().getCoachId());
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next()) {
                    coach_comments_data.add(new Coach_Comments_Data(
                            rs.getInt("評論編號"),
                            rs.getDate("評論日期"),
                            rs.getBytes("使用者圖片"),
                            rs.getString("使用者姓名"),
                            rs.getInt("評分"),
                            rs.getString("課程名稱"),
                            rs.getString("評論內容"),
                            rs.getString("回覆")
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
        RecyclerView recyclerView = findViewById(R.id.coachCommentsRecycleview);
        TextView emptyView = findViewById(R.id.empty_view);

        if (coach_comments_data.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            CoachCommentsAdapter adapter = new CoachCommentsAdapter(this, coach_comments_data, findViewById(R.id.main));
            recyclerView.setAdapter(adapter);
        }
    }

    static class Coach_Comments_Data {
        Date date;
        int commentID,rating;
        byte[] userimage;
        String userName, className, classNote,coachReply;
        static ArrayList<Coach_Comments_Data> coach_comments_data = new ArrayList<>();

        public Coach_Comments_Data(int commentID,Date date,byte[] userimage,String userName,int rating,String className,String classNote,String coachReply) {
            this.commentID=commentID;
            this.date=date;
            this.userimage=userimage;
            this.userName=userName;
            this.rating=rating;
            this.className=className;
            this.classNote=classNote;
            this.coachReply=coachReply;
        }
        public static ArrayList<Coach_Comments_Data> getCoach_comments_data() {
            if (coach_comments_data == null) {
                return null;
            }
            return coach_comments_data;
        }
        public int getCommentID(){return commentID;}
        private Date getDate(){return date;}
        private byte[] getUserimage(){return userimage;}
        private String getUserName(){return userName;}
        private int getRating(){return rating;}
        private String getClassName(){return className;}
        private String getClassNote(){return  classNote;}
        private String getCoachReply(){return coachReply;}

    }
    class CoachCommentsAdapter extends RecyclerView.Adapter<Coach_Comments.CoachCommentsAdapter.ViewHolder>
    {
        List<Coach_Comments.Coach_Comments_Data> coach_comments_data;
        Context context;
        View view;
        public CoachCommentsAdapter(Context context, List<Coach_Comments.Coach_Comments_Data> coach_comments_data, View view) {
            this.context = context;
            this.coach_comments_data = coach_comments_data;
            this.view=view;
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView user_image,edit_replyBtn;
            TextView comment_date,user_name,class_name,user_comment_info,coach_reply;
            RatingBar comment_rating;
            LinearLayout replyArea,edit_replyArea;
            EditText newReply;
            Button cancelBtn,updateBtn;
            public ViewHolder(View itemView) {
                super(itemView);
                user_image=itemView.findViewById(R.id.coach_comments_user_img);
                comment_date=itemView.findViewById(R.id.coach_comments_date);
                user_name=itemView.findViewById(R.id.coach_comments_username);
                class_name=itemView.findViewById(R.id.coach_comments_classname);
                user_comment_info=itemView.findViewById(R.id.coach_comments_info);
                coach_reply=itemView.findViewById(R.id.coach_comments_coachReply);
                comment_rating=itemView.findViewById(R.id.coach_comments_userRating);
                replyArea=itemView.findViewById(R.id.reply_area);
                edit_replyArea=itemView.findViewById(R.id.edit_reply_area);
                newReply=itemView.findViewById(R.id.coach_comments_replyText);
                edit_replyBtn=itemView.findViewById(R.id.coach_comments_showEditPanel);
                cancelBtn=itemView.findViewById(R.id.coach_comments_cancelreplyButton);
                updateBtn=itemView.findViewById(R.id.coach_comments_comfirmreplyButton);
            }
        }

        @NonNull
        @Override
        public CoachCommentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.coach_comments_item, parent, false);
            return new Coach_Comments.CoachCommentsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Coach_Comments.CoachCommentsAdapter.ViewHolder holder, int position) {
            Coach_Comments.Coach_Comments_Data item = coach_comments_data.get(position);

            if (item.getUserimage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getUserimage());
                holder.user_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            }
            holder.comment_date.setText(item.getDate().toString());
            holder.user_name.setText(item.getUserName());
            holder.class_name.setText(item.getClassName());
            holder.user_comment_info.setText(item.getClassNote());
            holder.comment_rating.setRating(item.getRating());
            try {
                String query = " SELECT 回覆 FROM [查看評論] WHERE [健身教練編號] = ?" ;
                PreparedStatement Statement = MyConnection.prepareStatement(query);
                Statement.setInt(1, Coach.getInstance().getCoachId());
                ResultSet rs = Statement.executeQuery();
                if (rs.next()) {
                    if (item.getCoachReply() != null && !item.getCoachReply().trim().isEmpty()) {
                        holder.newReply.setText(item.getCoachReply());
                        holder.updateBtn.setText("修改");
                        holder.edit_replyArea.setVisibility(View.GONE);
                        holder.coach_reply.setText(item.getCoachReply());
                    } else {
                        holder.updateBtn.setText("回覆");
                        holder.replyArea.setVisibility(View.GONE);
                        holder.cancelBtn.setVisibility(View.GONE);
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            holder.edit_replyBtn.setOnClickListener(v -> {
                holder.edit_replyArea.setVisibility(View.VISIBLE);
            });
            holder.cancelBtn.setOnClickListener(v -> {
                holder.edit_replyArea.setVisibility(View.GONE);
                holder.newReply.setText(item.getCoachReply());
            });
            holder.updateBtn.setOnClickListener(v -> {
                updateReply(holder.newReply.getText().toString().trim(),item.getCommentID(), position);
            });
        }
        private void updateReply(String reply, int comment_ID,int position) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                    String SQL = "UPDATE [完成預約評論表] SET [回覆] = ? WHERE [評論編號] = ?";
                    PreparedStatement Statement = MyConnection.prepareStatement(SQL);
                    Statement.setString(1, reply);
                    Statement.setInt(2, comment_ID);
                    int rowsAffected = Statement.executeUpdate();
                    if (rowsAffected > 0) {
                        coach_comments_data.remove(position);
                        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "回覆已更新", Toast.LENGTH_SHORT).show());
                        fetchComments();
                    }
                    Statement.close();
                } catch (SQLException e) {
                    Log.e("SQL", Objects.requireNonNull(e.getMessage()));
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "取消失敗", Toast.LENGTH_SHORT).show());
                }
            });
        }
        @Override
        public int getItemCount() {
            return coach_comments_data.size();
        }
    }
}
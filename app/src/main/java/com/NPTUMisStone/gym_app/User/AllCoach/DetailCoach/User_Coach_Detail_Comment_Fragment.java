package com.NPTUMisStone.gym_app.User.AllCoach.DetailCoach;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.AllClass.DetailClass.User_Class_Detail;
import com.NPTUMisStone.gym_app.User.AllClass.User_All_Class;
import com.NPTUMisStone.gym_app.User.AllCoach.User_All_Coach;
import com.NPTUMisStone.gym_app.User.Like.User_Like;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User.Records.AppointmentAll;
import com.NPTUMisStone.gym_app.User.Search.GymList;
import com.NPTUMisStone.gym_app.User_And_Coach.AIInteractive;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.databinding.UserCoachDetailClassFragmentBinding;
import com.NPTUMisStone.gym_app.databinding.UserCoachDetailCommentFragmentBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class User_Coach_Detail_Comment_Fragment extends Fragment {
    private UserCoachDetailCommentFragmentBinding binding;
    private Connection MyConnection;
    int coachID;
    TextView averageRating,commentCount;
    Button newestbtn,highestbtn,lowestbtn,mybtn;
    ProgressBar star5,star4,star3,star2,star1;
    public int User_Coach_Detail_commentID;
    boolean exists = false;
    ArrayList<User_Coach_Detail_Comment_Fragment.User_Coach_Detail_Comment_Data> commentData = new ArrayList<>();

    public static User_Coach_Detail_Comment_Fragment newInstance() {
        return new User_Coach_Detail_Comment_Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding= UserCoachDetailCommentFragmentBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        if (getArguments() != null) {
            coachID = getArguments().getInt("coachID");
        }
        BindId();
        MyConnection=new SQLConnection(binding.getRoot()).IWantToConnection();

        try {
            String query = "select 平均評分,評論數量 from [健身教練-評分] where 健身教練編號 = ? " ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,coachID);
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
                if(rs.getInt("評論數量")==0){
                    averageRating.setText("0.0 顆星");
                    commentCount.setText("0則評論");
                }
                else {
                    averageRating.setText(rs.getDouble("平均評分")+" 顆星");
                    commentCount.setText(rs.getInt("評論數量")+"則評論");
                }
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            String query = "SELECT COUNT(*) AS comment_count, "
                    + "SUM(CASE WHEN 評分 = 1 THEN 1 ELSE 0 END) AS star1_count, "
                    + "SUM(CASE WHEN 評分 = 2 THEN 1 ELSE 0 END) AS star2_count, "
                    + "SUM(CASE WHEN 評分 = 3 THEN 1 ELSE 0 END) AS star3_count, "
                    + "SUM(CASE WHEN 評分 = 4 THEN 1 ELSE 0 END) AS star4_count, "
                    + "SUM(CASE WHEN 評分 = 5 THEN 1 ELSE 0 END) AS star5_count "
                    + "FROM 查看評論 WHERE 健身教練編號 = ?" ;
            PreparedStatement Statement = MyConnection.prepareStatement(query);
            Statement.setInt(1,coachID);
            ResultSet rs = Statement.executeQuery();
            while (rs.next()) {
                int commentnum = rs.getInt("comment_count");
                int star1Count = rs.getInt("star1_count");
                int star2Count = rs.getInt("star2_count");
                int star3Count = rs.getInt("star3_count");
                int star4Count = rs.getInt("star4_count");
                int star5Count = rs.getInt("star5_count");
                star5.setMax(commentnum);
                star4.setMax(commentnum);
                star3.setMax(commentnum);
                star2.setMax(commentnum);
                star1.setMax(commentnum);
                star5.setProgress(star5Count);
                star4.setProgress(star4Count);
                star3.setProgress(star3Count);
                star2.setProgress(star2Count);
                star1.setProgress(star1Count);
            }
            rs.close();
            Statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        fetchComment();
        newestbtn.setOnClickListener(this::commentfilterclick);
        highestbtn.setOnClickListener(this::commentfilterclick);
        lowestbtn.setOnClickListener(this::commentfilterclick);
        mybtn.setOnClickListener(this::commentfilterclick);
        return root;
    }
    public void commentfilterclick(View view) {
        int id = view.getId();
        try {
            if (id == R.id.newest_comment_btn)
            {
                fetchComment();
                newestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#03A9F4")));
                highestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
                lowestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
                mybtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
            }
            else if (id == R.id.highest_comment_btn)
            {
                highComment();
                highestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#03A9F4")));
                newestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
                lowestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
                mybtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
            }
            else if (id == R.id.lowest_comment_btn)
            {
                lowComment();
                lowestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#03A9F4")));
                newestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
                highestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
                mybtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
            }
            else if (id == R.id.my_comment_btn)
            {
                MyComment();
                mybtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#03A9F4")));
                newestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
                highestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
                lowestbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D7D7D7")));
            }
        } catch (Exception e) {
            Log.e("Button", "Button click error", e);
        }
    }
    private void fetchComment() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                commentData.clear();
                String sql = "SELECT * FROM [查看評論] WHERE 健身教練編號 = ? ORDER BY 評論日期 DESC, 評論時間 DESC";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, coachID);
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    commentData.add(new User_Coach_Detail_Comment_Fragment.User_Coach_Detail_Comment_Data(
                            rs.getInt("使用者編號"),
                            rs.getInt("評論編號"),
                            rs.getInt("評分"),
                            rs.getBytes("使用者圖片"),
                            rs.getString("使用者姓名"),
                            rs.getString("評論日期"),
                            rs.getString("評論內容"),
                            rs.getString("課程名稱"),
                            rs.getString("回覆")
                    ));
                rs.close();
                searchStatement.close();
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
            }
            new Handler(Looper.getMainLooper()).post(this::updateUI);
        });
    }
    private void highComment() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                commentData.clear();
                String sql = "SELECT * FROM [查看評論] WHERE 健身教練編號 = ? ORDER BY 評分 DESC , 評論時間 DESC";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, coachID);
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    commentData.add(new User_Coach_Detail_Comment_Fragment.User_Coach_Detail_Comment_Data(
                            rs.getInt("使用者編號"),
                            rs.getInt("評論編號"),
                            rs.getInt("評分"),
                            rs.getBytes("使用者圖片"),
                            rs.getString("使用者姓名"),
                            rs.getString("評論日期"),
                            rs.getString("評論內容"),
                            rs.getString("課程名稱"),
                            rs.getString("回覆")
                    ));
                rs.close();
                searchStatement.close();
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
            }
            new Handler(Looper.getMainLooper()).post(this::updateUI);
        });
    }
    private void lowComment() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                commentData.clear();
                String sql = "SELECT * FROM [查看評論] WHERE 健身教練編號 = ? ORDER BY 評分 , 評論時間 DESC ";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, coachID);
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    commentData.add(new User_Coach_Detail_Comment_Fragment.User_Coach_Detail_Comment_Data(
                            rs.getInt("使用者編號"),
                            rs.getInt("評論編號"),
                            rs.getInt("評分"),
                            rs.getBytes("使用者圖片"),
                            rs.getString("使用者姓名"),
                            rs.getString("評論日期"),
                            rs.getString("評論內容"),
                            rs.getString("課程名稱"),
                            rs.getString("回覆")
                    ));
                rs.close();
                searchStatement.close();
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
            }
            new Handler(Looper.getMainLooper()).post(this::updateUI);
        });
    }
    private void MyComment() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                commentData.clear();
                String sql = "SELECT * FROM 查看評論 where 健身教練編號 = ? AND 使用者編號 = ? ORDER BY 評論日期 DESC, 評論時間 DESC";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, coachID);
                searchStatement.setInt(2, User.getInstance().getUserId());
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    commentData.add(new User_Coach_Detail_Comment_Fragment.User_Coach_Detail_Comment_Data(
                            rs.getInt("使用者編號"),
                            rs.getInt("評論編號"),
                            rs.getInt("評分"),
                            rs.getBytes("使用者圖片"),
                            rs.getString("使用者姓名"),
                            rs.getString("評論日期"),
                            rs.getString("評論內容"),
                            rs.getString("課程名稱"),
                            rs.getString("回覆")
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
            User_Coach_Detail_Comment_Fragment.Detail_Coach_CommentAdapter detailCoachCommentAdapter = new User_Coach_Detail_Comment_Fragment.Detail_Coach_CommentAdapter(getActivity(),commentData,binding.getRoot());
            RecyclerView coachDetailCommentRecyclerView= binding.userCoachDetailCommentRecyclerview;
            coachDetailCommentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            coachDetailCommentRecyclerView.setAdapter(detailCoachCommentAdapter);


        }
    }
    private void BindId(){
        averageRating=binding.averageRating;
        commentCount=binding.commentcount;
        star1=binding.star1;
        star2=binding.star2;
        star3=binding.star3;
        star4=binding.star4;
        star5=binding.star5;
        newestbtn=binding.newestCommentBtn;
        highestbtn=binding.highestCommentBtn;
        lowestbtn=binding.lowestCommentBtn;
        mybtn=binding.myCommentBtn;
    }
    static class User_Coach_Detail_Comment_Data {
        int userId,commentID,rating;
        byte[] userimage;
        String userName,commentdate,comment,className,coach_response;


        static ArrayList<User_Coach_Detail_Comment_Fragment.User_Coach_Detail_Comment_Data> commentData = new ArrayList<>();

        public User_Coach_Detail_Comment_Data(int userId,int commentID,int rating,byte[] userimage,String userName,String commentdate,String comment,String className,String coach_response) {
            this.userId=userId;
            this.commentID=commentID;
            this.rating=rating;
            this.userimage=userimage;
            this.userName=userName;
            this.commentdate=commentdate;
            this.comment=comment;
            this.className=className;
            this.coach_response=coach_response;

        }
        public static ArrayList<User_Coach_Detail_Comment_Fragment.User_Coach_Detail_Comment_Data> getCommentData() {
            if (commentData == null) {
                return null;
            }
            return commentData;
        }

        private int getUserId(){return userId;}
        private int getCommentID(){return commentID;}
        private int getRating(){return rating;}
        private byte[] getUserimage(){return userimage;}
        private String getUserName(){return userName;}
        private String getCommentdate(){return commentdate;}
        private String getComment(){return comment;}
        private String getClassName(){return className;}
        private String getCoach_response(){return coach_response;}

    }

    class Detail_Coach_CommentAdapter extends RecyclerView.Adapter<User_Coach_Detail_Comment_Fragment.Detail_Coach_CommentAdapter.ViewHolder>
    {
        List<User_Coach_Detail_Comment_Fragment.User_Coach_Detail_Comment_Data> comment_dataList;
        Context context;
        View view;
        public Detail_Coach_CommentAdapter(Context context, List<User_Coach_Detail_Comment_Fragment.User_Coach_Detail_Comment_Data> comment_dataList , View view) {
            this.context = context;
            this.comment_dataList = comment_dataList;
            this.view=view;
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView user_image;
            TextView userName,commentdate,comment,className,coach_response;
            RatingBar ratingBar;
            LinearLayout coachresponsearea;
            public ViewHolder(View itemView) {
                super(itemView);
                user_image=itemView.findViewById(R.id.user_coach_detail_comment_userimg);
                userName=itemView.findViewById(R.id.user_coach_detail_comment_username);
                commentdate=itemView.findViewById(R.id.user_coach_detail_comment_date);
                comment=itemView.findViewById(R.id.user_coach_detail_comment_usercomment);
                className=itemView.findViewById(R.id.user_coach_detail_comment_classname);
                coach_response=itemView.findViewById(R.id.user_coach_detail_comment_coachresponse);
                ratingBar=itemView.findViewById(R.id.user_coach_detail_comment_RatingBar);
                coachresponsearea=itemView.findViewById(R.id.user_coach_detail_comment_response);

            }
        }
        @NonNull
        @Override
        public User_Coach_Detail_Comment_Fragment.Detail_Coach_CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_coach_detail_comment_fragment_item, parent, false);
            return new User_Coach_Detail_Comment_Fragment.Detail_Coach_CommentAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull User_Coach_Detail_Comment_Fragment.Detail_Coach_CommentAdapter.ViewHolder holder, int position) {
            User_Coach_Detail_Comment_Fragment.User_Coach_Detail_Comment_Data item = comment_dataList.get(position);

            if (item.getUserimage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getUserimage());
                holder.user_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            }
            holder.userName.setText(item.getUserName());
            holder.commentdate.setText(item.getCommentdate());
            holder.comment.setText(item.getComment());
            holder.className.setText("課程名稱："+item.getClassName());
            holder.coach_response.setText(item.getCoach_response());
            holder.ratingBar.setRating(item.getRating());
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                String SQL = "select 回覆 from 查看評論 where 評論編號= ?";
                PreparedStatement Statement = MyConnection.prepareStatement(SQL);
                Statement.setInt(1,item.getCommentID());
                ResultSet rs = Statement.executeQuery();
                while (rs.next()) {
                    User_Coach_Detail_commentID=item.getCommentID();
                    Has_reply(User_Coach_Detail_commentID);
                    if(exists){
                        holder.coachresponsearea.setVisibility(View.VISIBLE);
                    }
                    else {
                        holder.coachresponsearea.setVisibility(View.GONE);
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        private boolean Has_reply(int comment_id)
        {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                String SQL = "select 回覆 from 查看評論 where 評論編號= ?";
                PreparedStatement Statement = MyConnection.prepareStatement(SQL);
                Statement.setInt(1,comment_id);
                ResultSet rs = Statement.executeQuery();
                while (rs.next()) {
                    if (rs.getString("回覆") != null)
                    {
                        exists = true;
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return exists;
        }
        @Override
        public int getItemCount() {
            return comment_dataList.size();
        }
    }


}
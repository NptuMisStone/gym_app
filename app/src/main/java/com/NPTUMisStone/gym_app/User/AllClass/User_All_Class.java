package com.NPTUMisStone.gym_app.User.AllClass;

import android.content.Context;
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
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class User_All_Class extends AppCompatActivity {

    Connection MyConnection;
    ArrayList<User_All_Class.User_All_Class_Data> class_data =new ArrayList<>();
    private All_ClassAdapter classAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_all_class);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fetchClass();
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
    }
    private void fetchClass() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                class_data.clear();
                String sql = "SELECT * FROM [健身教練課程-有排課的] ";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    class_data.add(new User_All_Class.User_All_Class_Data(
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

        User_All_Class.All_ClassAdapter ClassAdapter = new User_All_Class.All_ClassAdapter(this,class_data,findViewById(R.id.main));
        RecyclerView classRecyclerView = findViewById(R.id.userClassRecycleview);
        classRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        classRecyclerView.setAdapter(ClassAdapter);
    }

    static class User_All_Class_Data {
        int classID;
        byte[] classimage;
        String className,classPrice,coachName,classIntro,classPeople;

        static ArrayList<User_All_Class.User_All_Class_Data> classData = new ArrayList<>();

        public User_All_Class_Data(int classID,byte[] classimage,String className,String classPrice,String coachName,String classIntro,String classPeople) {
            this.classID=classID;
            this.classimage=classimage;
            this.className=className;
            this.classPrice=classPrice;
            this.coachName = coachName;
            this.classIntro=classIntro;
            this.classPeople=classPeople;
        }
        public static ArrayList<User_All_Class.User_All_Class_Data> getClassData() {
            if (classData == null) {
                return null;
            }
            return classData;
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

    class All_ClassAdapter extends RecyclerView.Adapter<User_All_Class.All_ClassAdapter.ViewHolder>
    {

        List<User_All_Class.User_All_Class_Data> class_dataList;
        Context context;
        View view;
        public All_ClassAdapter(Context context, List<User_All_Class.User_All_Class_Data> class_dataList , View view) {
            this.context = context;
            this.class_dataList = class_dataList;
            this.view=view;
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView class_image;
            TextView class_people_sign,class_name,class_price,coach_name,class_intro,class_people;
            ImageButton like_class_btn;
            public ViewHolder(View itemView) {
                super(itemView);
                class_image=itemView.findViewById(R.id.user_all_class_img);
                class_people_sign=itemView.findViewById(R.id.user_all_class_people_sign);
                class_name=itemView.findViewById(R.id.user_all_class_classname);
                class_price=itemView.findViewById(R.id.user_all_class_price);
                coach_name=itemView.findViewById(R.id.user_all_class_coachname);
                class_intro=itemView.findViewById(R.id.user_all_class_intro);
                class_people=itemView.findViewById(R.id.user_all_class_people);

                like_class_btn=itemView.findViewById(R.id.user_like_all_class_btn);
            }
        }
        @NonNull
        @Override
        public User_All_Class.All_ClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_all_class_item, parent, false);
            return new User_All_Class.All_ClassAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull User_All_Class.All_ClassAdapter.ViewHolder holder, int position) {
            User_All_Class.User_All_Class_Data item = class_dataList.get(position);

            if (item.getClassimage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getClassimage());
                holder.class_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            }
            holder.class_name.setText(item.getClassName());
            holder.class_price.setText("$"+item.getClassPrice().split("\\.")[0]+"/堂");
            holder.coach_name.setText(item.getCoachName());
            holder.class_intro.setText(item.getClassIntro());
            holder.class_people.setText("人數："+item.getClassPeople()+"人");
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                String sql = "SELECT COUNT(*) FROM 課程被收藏 WHERE 課程編號 = ? AND 使用者編號 = ?";
                PreparedStatement Statement = MyConnection.prepareStatement(sql);
                Statement.setInt(1,item.getClassID());
                Statement.setInt(2, User.getInstance().getUserId());
                ResultSet rs = Statement.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count > 0) {
                        holder.like_class_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like1));
                    } else {
                        holder.like_class_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dislike2));
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            holder.like_class_btn.setOnClickListener(v -> {
                Drawable currentDrawable = holder.like_class_btn.getDrawable();
                if (currentDrawable.getConstantState().equals(ContextCompat.getDrawable(context, R.drawable.dislike2).getConstantState())) {
                    // 如果当前是 dislike 状态，切换到 like
                    holder.like_class_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like1));

                    // 更新数据库，将课程标记为被收藏
                    try {
                        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                        String insertSql = "INSERT INTO 課程被收藏 (使用者編號, 課程編號) VALUES (?, ?)";
                        PreparedStatement insertStatement = MyConnection.prepareStatement(insertSql);
                        insertStatement.setInt(1, User.getInstance().getUserId());
                        insertStatement.setInt(2, item.getClassID());
                        insertStatement.executeUpdate();
                        insertStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 如果当前是 like 状态，切换到 dislike
                    holder.like_class_btn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.dislike2));
                    try {
                        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                        String deleteSql = "DELETE FROM 課程被收藏 WHERE 課程編號 = ? AND 使用者編號 = ?";
                        PreparedStatement deleteStatement = MyConnection.prepareStatement(deleteSql);
                        deleteStatement.setInt(1, item.getClassID());
                        deleteStatement.setInt(2, User.getInstance().getUserId());
                        deleteStatement.executeUpdate();
                        deleteStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            });
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                String SQL = "SELECT 上課人數 FROM 健身教練課程 WHERE 課程編號 = ? ";
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



        @Override
        public int getItemCount() {
            return class_dataList.size();
        }
    }
    public void  user_All_Class_goback(View view){
        finish();
    }
}
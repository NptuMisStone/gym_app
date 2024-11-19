package com.NPTUMisStone.gym_app.Coach.Records;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Coach_Appointment_Detail extends AppCompatActivity {
    Connection MyConnection;
    ArrayList<Coach_Detail_Ap_Data> detail_ap_data = new ArrayList<>();
    int scheduleID;
    TextView nodata;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_appointment_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
        // 返回按鈕
        findViewById(R.id.coach_Appointment_detail_back).setOnClickListener(v -> finish());
        scheduleID = getIntent().getIntExtra("看預約名單ID", 0);
        nodata=findViewById(R.id.nodata_coach_ap_detail);
        nodata.setVisibility(View.GONE);
        fetchAp();
    }
    private void fetchAp() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                detail_ap_data.clear();
                String sql = "SELECT * FROM [使用者預約-評論用] WHERE 課表編號= ? AND (預約狀態=1 OR  預約狀態=2) ";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, scheduleID);
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())
                    detail_ap_data.add(new Coach_Appointment_Detail.Coach_Detail_Ap_Data(
                            rs.getInt("課表編號"),
                            rs.getInt("預約編號"),
                            rs.getBytes("使用者圖片"),
                            rs.getString("預約狀態"),
                            rs.getString("使用者姓名"),
                            rs.getString("使用者性別"),
                            rs.getString("使用者電話"),
                            rs.getString("使用者郵件"),
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
        RecyclerView recyclerView = findViewById(R.id.coach_appopintment_detail_Recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Coach_Appointment_Detail.Coach_AP_Detail_Adapter adapter = new Coach_Appointment_Detail.Coach_AP_Detail_Adapter(this, detail_ap_data, findViewById(R.id.main));
        recyclerView.setAdapter(adapter);
        if(detail_ap_data==null){
            nodata.setVisibility(View.VISIBLE);
        }
    }
    static class Coach_Detail_Ap_Data {
        int scheduleID,appointmentID;
        byte[] userimage;
        String ap_status,user_name,user_gender,user_phone,user_mail,user_note;

        static ArrayList<Coach_Detail_Ap_Data> detail_ap_data = new ArrayList<>();

        public Coach_Detail_Ap_Data(int scheduleID,int appointmentID, byte[] userimage, String ap_status, String user_name, String user_gender, String user_phone, String user_mail,String user_note) {
            this.scheduleID=scheduleID;
            this.appointmentID=appointmentID;
            this.userimage=userimage;
            this.ap_status=ap_status;
            this.user_name=user_name;
            this.user_gender=user_gender;
            this.user_phone=user_phone;
            this.user_mail=user_mail;
            this.user_note=user_note;
        }

        public static ArrayList<Coach_Detail_Ap_Data> getClassData() {
            if (detail_ap_data == null) {
                return null;
            }
            return detail_ap_data;
        }


        public int getScheduleID(){return scheduleID;}
        public int getAppointmentID(){return appointmentID;}
        public byte[] getUserimage(){return userimage;}
        public String getAp_status() {
            return ap_status;
        }
        public String getUser_name() {
            return user_name;
        }
        public String getUser_gender() {
            return user_gender;
        }
        public String getUser_phone() {
            return user_phone;
        }
        public String getUser_mail() {
            return user_mail;
        }
        public String getUser_note() {
            return user_note;
        }
    }
    class Coach_AP_Detail_Adapter extends RecyclerView.Adapter<Coach_Appointment_Detail.Coach_AP_Detail_Adapter.ViewHolder> {

        List<Coach_Appointment_Detail.Coach_Detail_Ap_Data> detail_ap_data;
        Context context;
        View view;

        public Coach_AP_Detail_Adapter(Context context, List<Coach_Appointment_Detail.Coach_Detail_Ap_Data> detail_ap_data, View view) {
            this.context = context;
            this.detail_ap_data = detail_ap_data;
            this.view = view;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView user_image;
            TextView user_name, user_gender, user_phone, user_mail, user_note;
            Button cancelApBtn,finishApBtn;
            public ViewHolder(View itemView) {
                super(itemView);
                user_image=itemView.findViewById(R.id.coach_ap_detail_userimg);
                user_name=itemView.findViewById(R.id.coach_ap_detail_username);
                user_gender=itemView.findViewById(R.id.coach_ap_detail_usergender);
                user_phone=itemView.findViewById(R.id.coach_ap_detail_phone);
                user_mail=itemView.findViewById(R.id.coach_ap_detail_mail);
                user_note=itemView.findViewById(R.id.coach_ap_detail_note);
                cancelApBtn=itemView.findViewById(R.id.coach_ap_detail_cancelApButton);
                finishApBtn=itemView.findViewById(R.id.coach_ap_detail_finishApButton);
            }
        }

        @NonNull
        @Override
        public Coach_Appointment_Detail.Coach_AP_Detail_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.coach_appointment_detail_item, parent, false);
            return new Coach_Appointment_Detail.Coach_AP_Detail_Adapter.ViewHolder(view);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void onBindViewHolder(@NonNull Coach_Appointment_Detail.Coach_AP_Detail_Adapter.ViewHolder holder, int position) {
            Coach_Appointment_Detail.Coach_Detail_Ap_Data item = detail_ap_data.get(position);

            if (item.getUserimage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getUserimage());
                holder.user_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            }
            holder.user_name.setText("會員名稱："+item.getUser_name());
            switch (item.getUser_gender()){
                case "1":
                    holder.user_gender.setText("性別：男性");
                    break;
                case "2":
                    holder.user_gender.setText("性別：女性");
                    break;
                case "3":
                    holder.user_gender.setText("性別：不願透漏");
                    break;
            }
            holder.user_phone.setText("電話："+item.getUser_phone());
            holder.user_mail.setText("郵件："+item.getUser_mail());
            holder.user_note.setText("備註："+item.getUser_note());
            if(item.getAp_status().equals("2")){
                holder.cancelApBtn.setVisibility(View.GONE);
                holder.finishApBtn.setVisibility(View.GONE);
            }
            holder.cancelApBtn.setOnClickListener(v -> {
                try {
                    MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                    String sql = "UPDATE 使用者預約 SET 預約狀態 = 5 WHERE 預約編號 = ? ";
                    PreparedStatement Statement = MyConnection.prepareStatement(sql);
                    Statement.setInt(1, detail_ap_data.get(position).getAppointmentID());
                    int rowsAffected = Statement.executeUpdate();
                    if (rowsAffected > 0) {
                        UpdatePeople(detail_ap_data.get(position).getScheduleID());
                        detail_ap_data.remove(position);
                        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "已取消", Toast.LENGTH_SHORT).show());
                        fetchAp();
                    }
                    Statement.close();
                } catch (SQLException e) {
                    Log.e("SQL", "預約狀態=5", e);
                }
            });
            holder.finishApBtn.setOnClickListener(v -> {
                try {
                    MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                    String sql = "UPDATE 使用者預約 SET 預約狀態 = 2 WHERE 預約編號 = ? ";
                    PreparedStatement Statement = MyConnection.prepareStatement(sql);
                    Statement.setInt(1, detail_ap_data.get(position).getAppointmentID());
                    int rowsAffected = Statement.executeUpdate();
                    if (rowsAffected > 0) {
                        FinishPeople();
                        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "已更新", Toast.LENGTH_SHORT).show());
                        fetchAp();
                    }
                    Statement.close();
                } catch (SQLException e) {
                    Log.e("SQL", "預約狀態=2", e);
                }
            });
        }
        private void UpdatePeople(int scheduleID) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    // 更新 SQL
                    String query = "UPDATE [健身教練課表] SET 預約人數 = ([預約人數]-1) WHERE 課表編號 = ?";
                    PreparedStatement updateStatement = MyConnection.prepareStatement(query);
                    updateStatement.setInt(1, scheduleID);
                    updateStatement.executeUpdate();
                    // 關閉資源
                    updateStatement.close();
                } catch (SQLException e) {
                    Log.e("SQL", Objects.requireNonNull(e.getMessage()));
                }
            });
        }
        private void FinishPeople() {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    // 更新 SQL
                    String query = "UPDATE 健身教練資料 SET 健身教練次數 = ([健身教練次數] + 1) WHERE 健身教練編號 = ? ";
                    PreparedStatement updateStatement = MyConnection.prepareStatement(query);
                    updateStatement.setInt(1, Coach.getInstance().getCoachId());
                    updateStatement.executeUpdate();
                    // 關閉資源
                    updateStatement.close();
                } catch (SQLException e) {
                    Log.e("SQL", Objects.requireNonNull(e.getMessage()));
                }
            });
        }
        @Override
        public int getItemCount() {
            return detail_ap_data.size();
        }
    }
    public  void coach_Appointment_detail_goback(View view){
        finish();
    }
}
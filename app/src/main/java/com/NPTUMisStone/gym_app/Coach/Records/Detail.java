package com.NPTUMisStone.gym_app.Coach.Records;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Main.Identify.JavaMailAPI;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;
import com.NPTUMisStone.gym_app.User_And_Coach.Map.Redirecting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Detail extends AppCompatActivity {
    Connection MyConnection;
    ArrayList<Coach_Detail_Ap_Data> detail_ap_data = new ArrayList<>();

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
        findViewById(R.id.AppointmentDetail_detailText).setVisibility(View.GONE);
        fetchAppointment();
    }

    private void fetchAppointment() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                detail_ap_data.clear();
                String sql = "SELECT * FROM [使用者預約-評論用] WHERE 課表編號= ? AND (預約狀態=1 OR  預約狀態=2) ";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, getIntent().getIntExtra("看預約名單ID", 0));
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())
                    detail_ap_data.add(new Detail.Coach_Detail_Ap_Data(
                            rs.getInt("課表編號"),
                            rs.getInt("預約編號"),
                            rs.getBytes("使用者圖片"),
                            rs.getString("預約狀態"),
                            rs.getString("使用者姓名"),
                            rs.getString("使用者性別"),
                            rs.getString("使用者電話"),
                            rs.getString("使用者郵件"),
                            rs.getString("備註"),
                            rs.getString("地點名稱"),
                            rs.getInt("課程編號")
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
        RecyclerView recyclerView = findViewById(R.id.AppointmentDetail_detailRecycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Detail.Coach_AP_Detail_Adapter adapter = new Detail.Coach_AP_Detail_Adapter(this, detail_ap_data, findViewById(R.id.main));
        recyclerView.setAdapter(adapter);
        if (detail_ap_data == null) {
            findViewById(R.id.AppointmentDetail_detailText).setVisibility(View.VISIBLE);
        }
    }

    static class Coach_Detail_Ap_Data {
        int scheduleID, appointmentID, classID;
        byte[] userImage;
        String ap_status, user_name, user_gender, user_phone, user_mail, user_note, user_location;

        static ArrayList<Coach_Detail_Ap_Data> detail_ap_data = new ArrayList<>();

        public Coach_Detail_Ap_Data(int scheduleID, int appointmentID, byte[] userImage, String ap_status, String user_name, String user_gender, String user_phone, String user_mail, String user_note, String user_location, int classID) {
            this.scheduleID = scheduleID;
            this.appointmentID = appointmentID;
            this.userImage = userImage;
            this.ap_status = ap_status;
            this.user_name = user_name;
            this.user_gender = user_gender;
            this.user_phone = user_phone;
            this.user_mail = user_mail;
            this.user_note = user_note;
            this.user_location = user_location;
            this.classID = classID;
        }

        public static ArrayList<Coach_Detail_Ap_Data> getClassData() {
            if (detail_ap_data == null) {
                return null;
            }
            return detail_ap_data;
        }


        public int getScheduleID() {
            return scheduleID;
        }

        public int getAppointmentID() {
            return appointmentID;
        }

        public byte[] getUserImage() {
            return userImage;
        }

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
        public String getUser_location() {
            return user_location;
        }
        public int getClassID() {
            return classID;
        }
    }

    class Coach_AP_Detail_Adapter extends RecyclerView.Adapter<Detail.Coach_AP_Detail_Adapter.ViewHolder> {

        List<Detail.Coach_Detail_Ap_Data> detail_ap_data;
        Context context;
        View view;

        public Coach_AP_Detail_Adapter(Context context, List<Detail.Coach_Detail_Ap_Data> detail_ap_data, View view) {
            this.context = context;
            this.detail_ap_data = detail_ap_data;
            this.view = view;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView user_image;
            TextView user_name, user_gender, user_phone, user_mail, user_note, user_location;
            Button cancelApBtn, finishApBtn;
            ImageButton directionButton;

            public ViewHolder(View itemView) {
                super(itemView);
                user_image = itemView.findViewById(R.id.AppointmentDetail_classImage);
                user_name = itemView.findViewById(R.id.AppointmentDetail_nameText);
                user_gender = itemView.findViewById(R.id.AppointmentDetail_sexText);
                user_phone = itemView.findViewById(R.id.AppointmentDetail_phoneText);
                user_mail = itemView.findViewById(R.id.AppointmentDetail_mailText);
                user_note = itemView.findViewById(R.id.AppointmentDetail_noteText);
                user_location = itemView.findViewById(R.id.AppointmentDetail_locationText);
                cancelApBtn = itemView.findViewById(R.id.AppointmentDetail_cancelButton);
                finishApBtn = itemView.findViewById(R.id.AppointmentDetail_finishButton);
                directionButton = itemView.findViewById(R.id.AppointmentDetail_directionButton);
            }
        }

        @NonNull
        @Override
        public Detail.Coach_AP_Detail_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.coach_appointment_detail_item, parent, false);
            return new Detail.Coach_AP_Detail_Adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Detail.Coach_AP_Detail_Adapter.ViewHolder holder, int position) {
            Detail.Coach_Detail_Ap_Data item = detail_ap_data.get(position);

            if (item.getUserImage() != null) {
                holder.user_image.setImageBitmap(ImageHandle.resizeBitmap(ImageHandle.getBitmap(item.getUserImage())));
            }
            if (item.getAp_status().equals("2")) {
                holder.cancelApBtn.setVisibility(View.GONE);
                holder.finishApBtn.setVisibility(View.GONE);
            }
            switch (item.getUser_gender()) {
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
            holder.user_name.setText("會員名稱：" + item.getUser_name());
            holder.user_phone.setText("電話：" + item.getUser_phone());
            holder.user_mail.setText("郵件：" + item.getUser_mail());
            holder.user_note.setText("備註：" + item.getUser_note());
            holder.user_location.setText("地點名稱：" + item.getUser_location());
            holder.directionButton.setOnClickListener(v -> new Redirecting(Detail.this, Redirecting.getLocationAddress(MyConnection,item.getClassID())).getCurrentLocation());
            if (holder.cancelApBtn != null) {
                holder.cancelApBtn.setOnClickListener(v -> {
                    try {
                        if (!isCancelable(detail_ap_data.get(position).getScheduleID())) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(context, "❌ 無法取消：需於課程開始 24 小時前取消", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        new AlertDialog.Builder(context)
                                .setTitle("確認取消")
                                .setMessage("您確定要取消此課程嗎？")
                                .setPositiveButton("確認", (dialog, which) -> cancelAppointment(position))
                                .setNegativeButton("取消", null)
                                .show();
                    } catch (Exception e) {
                        Log.e("SQL", "Error during cancellation logic", e);
                    }
                });
            }

            if (holder.finishApBtn != null) {
                holder.finishApBtn.setOnClickListener(v -> {
                    try {
                        if (!isCompletable(detail_ap_data.get(position).getScheduleID())) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(context, "❌ 課程尚未結束，無法標記為完成", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        new Handler(Looper.getMainLooper()).post(() -> {
                            try {
                                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                                String sql = "UPDATE 使用者預約 SET 預約狀態 = 2 WHERE 預約編號 = ? ";
                                PreparedStatement Statement = MyConnection.prepareStatement(sql);
                                Statement.setInt(1, detail_ap_data.get(position).getAppointmentID());
                                int rowsAffected = Statement.executeUpdate();
                                if (rowsAffected > 0) {
                                    FinishPeople();
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "已更新", Toast.LENGTH_SHORT).show();
                                    fetchAppointment();
                                }
                                Statement.close();
                            } catch (SQLException e) {
                                Log.e("SQL", "預約狀態=2", e);
                            }
                        });
                    } catch (Exception e) {
                        Log.e("SQL", "Error during finish logic", e);
                    }
                });
            }
        }

        private boolean isCancelable(int scheduleId) {
            String query = "SELECT 開始時間, 日期 FROM 健身教練課表 WHERE 課表編號 = ?";
            try (PreparedStatement stmt = MyConnection.prepareStatement(query)) {
                stmt.setInt(1, scheduleId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String date = rs.getString("日期");
                    String startTime = rs.getString("開始時間");

                    // 解析課程開始時間
                    LocalDateTime classStart = LocalDateTime.parse(date + "T" + startTime);

                    // 獲取當前時間
                    LocalDateTime now = LocalDateTime.now();

                    // 判斷是否大於等於 24 小時
                    Duration duration = Duration.between(now, classStart);
                    return duration.toHours() >= 24;
                }
            } catch (SQLException e) {
                Log.e("DatabaseError", "Error checking cancelability", e);
            }
            return false; // 如果查詢失敗或其他錯誤，預設不可取消
        }

        private boolean isCompletable(int scheduleId) {
            String query = "SELECT 結束時間, 日期 FROM 健身教練課表 WHERE 課表編號 = ?";
            try (PreparedStatement stmt = MyConnection.prepareStatement(query)) {
                stmt.setInt(1, scheduleId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String date = rs.getString("日期"); // 假設格式為 yyyy-MM-dd
                    String endTime = rs.getString("結束時間"); // 假設格式為 HH:mm:ss

                    // 解析課程結束時間
                    LocalDate courseDate = LocalDate.parse(date);
                    LocalTime courseEndTime = LocalTime.parse(endTime);
                    LocalDateTime classEnd = LocalDateTime.of(courseDate, courseEndTime);

                    // 獲取當前時間
                    LocalDateTime now = LocalDateTime.now();

                    // 日誌記錄
                    Log.d("TimeCheck", "現在時間：" + now);
                    Log.d("TimeCheck", "課程結束時間：" + classEnd);

                    // 判斷是否已超過結束時間
                    return now.isAfter(classEnd);
                }
            } catch (SQLException e) {
                Log.e("DatabaseError", "Error checking completable status", e);
            } catch (DateTimeParseException e) {
                Log.e("TimeParseError", "Error parsing date or time", e);
            }
            return false; // 如果查詢失敗或其他錯誤，預設不可完成
        }

        private void cancelAppointment(int position) {
            try {
                String sql = "UPDATE 使用者預約 SET 預約狀態 = 5 WHERE 預約編號 = ?";
                PreparedStatement Statement = MyConnection.prepareStatement(sql);
                Statement.setInt(1, detail_ap_data.get(position).getAppointmentID());
                int rowsAffected = Statement.executeUpdate();
                if (rowsAffected > 0) {
                    // 更新課表人數
                    UpdatePeople(detail_ap_data.get(position).getScheduleID());

                    // 發送取消通知給使用者
                    sendCancellationNotificationToUser(
                            detail_ap_data.get(position).getScheduleID(),
                            detail_ap_data.get(position).getUser_mail()
                    );

                    // 更新資料
                    detail_ap_data.remove(position);
                    new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "已取消", Toast.LENGTH_SHORT).show());
                    fetchAppointment();
                }
                Statement.close();
            } catch (SQLException e) {
                Log.e("SQL", "Error during appointment cancellation", e);
            }
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

        private void sendCancellationNotificationToUser(int scheduleId, String userEmail) {
            String query = "SELECT 課程名稱, 日期, 開始時間, 結束時間, 健身教練姓名 " +
                    "FROM [使用者預約-有預約的] WHERE 課表編號 = ?";
            try (PreparedStatement stmt = MyConnection.prepareStatement(query)) {
                stmt.setInt(1, scheduleId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String courseDate = rs.getString("日期");
                    String startTime = rs.getString("開始時間");
                    String endTime = rs.getString("結束時間");
                    String courseName = rs.getString("課程名稱");
                    String coachName = rs.getString("健身教練姓名");

                    // 發送通知
                    sendCancellationNotification(userEmail, courseDate, startTime, endTime, courseName, coachName);
                }
            } catch (SQLException e) {
                Log.e("DatabaseError", "Error sending cancellation notification", e);
            }
        }

        private void sendCancellationNotification(String userEmail, String courseDate, String startTime, String endTime, String courseName, String coachName) {
            String subject = "【屏大Fit-健身預約系統】課程取消通知";
            String message = "您好，\n\n" +
                    "我們遺憾地通知您，您所預約的課程已被教練取消。\n\n" +
                    "課程詳細資訊如下：\n" +
                    "課程名稱：" + courseName + "\n" +
                    "課程日期：" + courseDate + "\n" +
                    "課程時間：" + startTime + " ~ " + endTime + "\n" +
                    "教練名稱：" + coachName + "\n\n" +
                    "若有任何問題，請聯繫教練或客服人員。\n\n" +
                    "屏大Fit 團隊";
            new JavaMailAPI(context, userEmail, subject, message).sendMail(new JavaMailAPI.EmailSendResultCallback() {
                @Override
                public void onSuccess() {
                    Log.d("EmailNotification", "通知已成功發送至：" + userEmail);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("EmailNotificationError", "通知發送失敗：" + userEmail, e);
                }
            });
        }


    }

    public void coach_Appointment_detail_goBack(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("刷新數據", true);
        setResult(RESULT_OK, resultIntent);
        super.finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (MyConnection == null || MyConnection.isClosed()) {
                MyConnection = new SQLConnection(findViewById(R.id.main)).IWantToConnection();
                notifyAll();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
package com.NPTUMisStone.gym_app.User.Records;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
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
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.databinding.UserAppointmentNowBinding;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Appointment_NowFragment extends Fragment {

    private UserAppointmentNowBinding binding;
    private Connection MyConnection;
    ArrayList<User_Now_AppointmentData> appointmentData = new ArrayList<>();
    private ProgressBar progressBar;
    TextView nodata;
    int apPeople;

    public static Appointment_NowFragment newInstance() {
        return new Appointment_NowFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Appointment_NowViewModel appointmentNowViewModel =new ViewModelProvider(this).get(Appointment_NowViewModel.class);

        binding=UserAppointmentNowBinding.inflate(inflater,container,false);
        nodata=binding.userApNodata;
        nodata.setVisibility(View.GONE);
        View root = binding.getRoot();

        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        fetchAppointments();
        return root;
    }
    private void fetchAppointments() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                appointmentData.clear();
                String sql = "SELECT 預約編號,日期,星期幾,開始時間,課程時間長度,課程名稱,課程費用,健身教練圖片,健身教練姓名,預約狀態,備註,課表編號 FROM [使用者預約-有預約的] WHERE [預約狀態] = 1 AND 使用者編號 = ?";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, User.getInstance().getUserId());
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    appointmentData.add(new User_Now_AppointmentData(
                            rs.getInt("預約編號"),
                            rs.getDate("日期"),
                            rs.getString("星期幾"),
                            rs.getString("開始時間"),
                            rs.getInt("課程時間長度"),
                            rs.getString("課程名稱"),
                            rs.getString("課程費用"),
                            rs.getBytes("健身教練圖片"),
                            rs.getString("健身教練姓名"),
                            rs.getInt("預約狀態"),
                            rs.getString("備註"),
                            rs.getInt("課表編號")
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
            NowAppointmentAdapter userAppointmentAdapter = new NowAppointmentAdapter(getActivity(),appointmentData,binding.getRoot());
            RecyclerView nowApRecyclerView = binding.userApNowRecycleview;
            nowApRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            nowApRecyclerView.setAdapter(userAppointmentAdapter);
            progressBar.setVisibility(View.GONE);
            if (appointmentData.isEmpty()) {
                nodata.setVisibility(View.VISIBLE);
            } else {
                nodata.setVisibility(View.GONE);
            }

        }
    }
    static class User_Now_AppointmentData {
        Date date;
        int reservationID, timeLong, status,scheduleID;
        byte[] coachimage;
        String className, classPrice, coachName, note,week,time;
        static ArrayList<User_Now_AppointmentData> appointments = new ArrayList<>();

        public User_Now_AppointmentData(int reservationID, Date date, String week, String time, int timeLong, String className, String classPrice, byte[] coachimage, String coachName, int status, String note,int scheduleID) {
            this.reservationID = reservationID;
            this.date = date;
            this.week=week;
            this.time=time;
            this.timeLong = timeLong;
            this.className = className;
            this.classPrice = classPrice;
            this.coachimage = coachimage;
            this.coachName = coachName;
            this.status = status;
            this.note = note;
            this.scheduleID=scheduleID;
        }
        public static ArrayList<User_Now_AppointmentData> getAppointments() {
            if (appointments == null) {
                return null;
            }
            return appointments;
        }

        private int getReservationID() {
            return reservationID;
        }

        private Date getDate() {
            return date;
        }
        private String getWeek(){return week;}

        private String getTime(){return  time;}

        private int getTimeLong() {
            return timeLong;
        }

        private String getClassName() {
            return className;
        }
        private String getClassPrice(){
            return classPrice;
        }

        private byte[] getCoachimage() {
            return coachimage;
        }

        private String getCoachName() {
            return coachName;
        }


        private String getNote() {
            return note;
        }

        private int getScheduleID() {
            return scheduleID;
        }
    }
    class NowAppointmentAdapter extends RecyclerView.Adapter<NowAppointmentAdapter.ViewHolder>
    {

        List<User_Now_AppointmentData> appointmentDataList;
        Context context;
        View view;
        public NowAppointmentAdapter(Context context, List<User_Now_AppointmentData> appointmentList,View view) {
            this.context = context;
            this.appointmentDataList = appointmentList;
            this.view=view;
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView coach_image;
            TextView ap_date, ap_Time, class_time_long, class_name, class_price, coach_name, note, ap_week;
            ImageButton user_ap_cancel_btn;
            public ViewHolder(View itemView) {
                super(itemView);
                coach_image = itemView.findViewById(R.id.user_ap_coach_img);
                ap_date = itemView.findViewById(R.id.user_ap_date);
                ap_week = itemView.findViewById(R.id.user_ap_week);
                ap_Time = itemView.findViewById(R.id.user_like_class_classname);
                class_time_long = itemView.findViewById(R.id.user_ap_timelong);
                class_name = itemView.findViewById(R.id.user_like_class_coachname);
                class_price = itemView.findViewById(R.id.user_ap_class_price);
                coach_name = itemView.findViewById(R.id.user_like_class_people);
                note = itemView.findViewById(R.id.user_like_coach_type);
                user_ap_cancel_btn=itemView.findViewById(R.id.user_ap_cancel_confirmbtn);
            }
        }

        @NonNull
        @Override
        public NowAppointmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_appointment_now_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NowAppointmentAdapter.ViewHolder holder, int position) {
            User_Now_AppointmentData item = appointmentDataList.get(position);

            if (item.getCoachimage() != null) {
                Bitmap bitmap = ImageHandle.getBitmap(item.getCoachimage());
                holder.coach_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
            }
            holder.ap_date.setText(item.getDate().toString());
            holder.ap_week.setText(item.getWeek());
            holder.ap_Time.setText(item.getTime());
            holder.class_time_long.setText(String.valueOf(item.getTimeLong()));
            holder.class_name.setText(item.getClassName());
            holder.class_price.setText(item.getClassPrice().split("\\.")[0]);
            holder.coach_name.setText(item.getCoachName());
            holder.note.setText(item.getNote());
            holder.user_ap_cancel_btn.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("確認取消")
                        .setMessage("確定要取消此預約嗎？")
                        .setPositiveButton("確認", (dialog, which) -> {
                            // 調用刪除方法
                            deleteAppointment(item.getReservationID(), position);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
        }
        private void deleteAppointment(int reservationID, int position) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                    String deleteSQL = "UPDATE 使用者預約 SET 預約狀態=3 WHERE 使用者編號= ? AND 預約編號 = ?";
                    PreparedStatement deleteStatement = MyConnection.prepareStatement(deleteSQL);
                    deleteStatement.setInt(1, User.getInstance().getUserId());
                    deleteStatement.setInt(2, reservationID);
                    int rowsAffected = deleteStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        // 刪除成功後更新列表
                        UpdatePeople(appointmentDataList.get(position).getScheduleID());
                        appointmentDataList.remove(position);
                        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(context, "預約已取消", Toast.LENGTH_SHORT).show());
                        fetchAppointments();
                    }
                    deleteStatement.close();
                } catch (SQLException e) {
                    Log.e("SQL", Objects.requireNonNull(e.getMessage()));
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "取消失敗", Toast.LENGTH_SHORT).show());
                }
            });
        }
        private int SearchAPPeople(int scheduleID){
                try {
                    MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();

                    String sql = "SELECT 預約人數 FROM [健身教練課表] WHERE 課表編號 = ?";
                    PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                    searchStatement.setInt(1,scheduleID);
                    ResultSet rs = searchStatement.executeQuery();
                    if (rs.next()) {
                        apPeople = rs.getInt("預約人數");
                        searchStatement.close();
                    }
                    rs.close();
                    searchStatement.close();
                } catch (SQLException e) {
                    Log.e("SQL", Objects.requireNonNull(e.getMessage()));
                }
            return  apPeople;
        }

        private void UpdatePeople(int scheduleID) {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    // 建立数据库连接
                    MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                    // 查询預約人數
                    int People = SearchAPPeople(scheduleID);

                    // 更新 SQL
                    String query = "UPDATE [健身教練課表] SET 預約人數 = ? WHERE 課表編號 = ?";
                    PreparedStatement updateStatement = MyConnection.prepareStatement(query);
                    updateStatement.setInt(1, (People-1));
                    updateStatement.setInt(2, scheduleID);
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
            return appointmentDataList.size();
        }
    }


}
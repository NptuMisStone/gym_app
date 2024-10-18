package com.NPTUMisStone.gym_app.User.Records.FinishAppointment;

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
import com.NPTUMisStone.gym_app.databinding.UserFragmentFinishAppointmentBinding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class FinishAppointmentFragment extends Fragment {

    private UserFragmentFinishAppointmentBinding binding;
    private Connection MyConnection;
    ArrayList<FinishAppointmentFragment.User_Finish_AppointmentData> appointmentData = new ArrayList<>();
    private ProgressBar progressBar;

    public static FinishAppointmentFragment newInstance() {
        return new FinishAppointmentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FinishAppointmentViewModel finishAppointmentViewModel=new ViewModelProvider(this).get(FinishAppointmentViewModel.class);
        binding=UserFragmentFinishAppointmentBinding.inflate(inflater,container,false);
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
                String sql = "SELECT 預約編號,日期,星期幾,開始時間,課程時間長度,課程名稱,課程費用,健身教練圖片,健身教練姓名,預約狀態,備註 FROM [使用者預約-有預約的] WHERE [預約狀態] = 2 AND 使用者編號 = ?";
                PreparedStatement searchStatement = MyConnection.prepareStatement(sql);
                searchStatement.setInt(1, User.getInstance().getUserId());
                ResultSet rs = searchStatement.executeQuery();
                while (rs.next())

                    appointmentData.add(new FinishAppointmentFragment.User_Finish_AppointmentData(
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
                            rs.getString("備註")
                    ));
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
            }
            new Handler(Looper.getMainLooper()).post(this::updateUI);
        });

    }
    private void updateUI() {
        if (getActivity() != null && isAdded()) {
            FinishAppointmentFragment.FinishAppointmentAdapter userAppointmentAdapter = new FinishAppointmentFragment.FinishAppointmentAdapter(getActivity(),appointmentData,binding.getRoot());
            RecyclerView finishApRecyclerView = binding.userApFinishRecycleview;
            finishApRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            finishApRecyclerView.setAdapter(userAppointmentAdapter);
            progressBar.setVisibility(View.GONE);

        }
    }
    static class User_Finish_AppointmentData {
        Date date;
        int reservationID, timeLong, status;
        byte[] coachimage;
        String className, classPrice, coachName, note,week,time;
        static ArrayList<com.NPTUMisStone.gym_app.User.Records.User_AppointmentData> appointments = new ArrayList<>();

        public User_Finish_AppointmentData(int reservationID, Date date, String week, String time, int timeLong, String className, String classPrice, byte[] coachimage, String coachName, int status, String note) {
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

        }
        public static ArrayList<com.NPTUMisStone.gym_app.User.Records.User_AppointmentData> getAppointments() {
            if (appointments == null) {
                return null;
            }
            return appointments;
        }

        public int getReservationID() {
            return reservationID;
        }

        public Date getDate() {
            return date;
        }
        public String getWeek(){return week;}

        public String getTime(){return  time;}

        public int getTimeLong() {
            return timeLong;
        }

        public String getClassName() {
            return className;
        }
        public String getClassPrice(){
            return classPrice;
        }

        public byte[] getCoachimage() {
            return coachimage;
        }

        public String getCoachName() {
            return coachName;
        }


        public String getNote() {
            return note;
        }

    }
    static  class FinishAppointmentAdapter extends RecyclerView.Adapter<FinishAppointmentFragment.FinishAppointmentAdapter.ViewHolder>
    {

        List<FinishAppointmentFragment.User_Finish_AppointmentData> appointmentDataList;
        Context context;
        View view;
        public FinishAppointmentAdapter(Context context, List<FinishAppointmentFragment.User_Finish_AppointmentData> appointmentList, View view) {
            this.context = context;
            this.appointmentDataList = appointmentList;
            this.view=view;
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView coach_image;
            TextView ap_date, ap_Time, class_time_long, class_name, class_price, coach_name, note, ap_week;
            public ViewHolder(View itemView) {
                super(itemView);
                coach_image = itemView.findViewById(R.id.user_ap_coach_img);
                ap_date = itemView.findViewById(R.id.user_ap_date);
                ap_week = itemView.findViewById(R.id.user_ap_week);
                ap_Time = itemView.findViewById(R.id.user_ap_time);
                class_time_long = itemView.findViewById(R.id.user_ap_timelong);
                class_name = itemView.findViewById(R.id.user_ap_class_name);
                class_price = itemView.findViewById(R.id.user_ap_class_price);
                coach_name = itemView.findViewById(R.id.user_ap_coach_name);
                note = itemView.findViewById(R.id.user_ap_note);
            }
        }

        @NonNull
        @Override
        public FinishAppointmentFragment.FinishAppointmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_finish_appointment_item, parent, false);
            return new FinishAppointmentFragment.FinishAppointmentAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FinishAppointmentFragment.FinishAppointmentAdapter.ViewHolder holder, int position) {
            FinishAppointmentFragment.User_Finish_AppointmentData item = appointmentDataList.get(position);

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

        }

        @Override
        public int getItemCount() {
            return appointmentDataList.size();
        }
    }
}
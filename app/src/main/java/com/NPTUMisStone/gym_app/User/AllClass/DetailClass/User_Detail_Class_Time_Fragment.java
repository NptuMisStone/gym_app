package com.NPTUMisStone.gym_app.User.AllClass.DetailClass;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.AllClass.User_Confirm_Appointment;
import com.NPTUMisStone.gym_app.User.Comments.User_Comments;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User.Records.NowAppointment.NowAppointmentFragment;
import com.NPTUMisStone.gym_app.User.Search.Calendar.CalendarFragment;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;
import com.NPTUMisStone.gym_app.databinding.UserDetailClassTimeFragmentBinding;
import com.hdev.calendar.bean.DateInfo;
import com.hdev.calendar.view.SingleCalendarView;

import net.sourceforge.jtds.jdbc.DateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class User_Detail_Class_Time_Fragment extends Fragment {

    private UserDetailClassTimeFragmentBinding binding;
    Connection MyConnection;
    private int classID;
    private List<java.sql.Date> courseDates;
    SingleCalendarView singleCalendarView;
    ArrayList<User_Detail_Class_Time_Fragment.User_Detail_Class_Time_Data> detailClassTimeData = new ArrayList<>();

    public static User_Detail_Class_Time_Fragment newInstance() {
        return new User_Detail_Class_Time_Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding=UserDetailClassTimeFragmentBinding.inflate(inflater,container,false);


        if (getArguments() != null) {
            classID = getArguments().getInt("classID");
        }
        courseDates =getDate(classID);

        singleCalendarView = binding.userDetailClassChooseDate;
        DateInfo startDate= new DateInfo();
        Calendar startcalendar = Calendar.getInstance();
        startDate.setYear(startcalendar.get(Calendar.YEAR));
        startDate.setMonth(startcalendar.get(Calendar.MONTH) + 1); // 月份從0開始，故+1
        startDate.setDay(startcalendar.get(Calendar.DAY_OF_MONTH));
        // 設置結束日期為courseDates中最大的日期(那個課程的最大日期)
        java.sql.Date maxDate = Collections.max(courseDates);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(maxDate);
        DateInfo endDate = new DateInfo(endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH) + 1,
                endCalendar.get(Calendar.DAY_OF_MONTH));
        // 日期範圍
        singleCalendarView.setDateRange(
                startDate.timeInMillis(),
                endDate.timeInMillis()
        );
        singleCalendarView.setOnSingleDateSelectedListener((calendar, date) -> {
            fetchTimeInfo(date);
            return null;
        });
        singleCalendarView.setSelectedDate(startDate);


        return binding.getRoot();

    }
    private void fetchTimeInfo(DateInfo date) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                detailClassTimeData.clear();
                String query = "SELECT * FROM 健身教練課表課程合併 WHERE 課程編號 = ? AND 日期 = ? ORDER BY [開始時間]";
                PreparedStatement preparedStatement = MyConnection.prepareStatement(query);
                preparedStatement.setInt(1, classID);
                preparedStatement.setString(2,date.getYear()+"-"+date.getMonth()+"-"+date.getDay());
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next())
                    detailClassTimeData.add(new User_Detail_Class_Time_Fragment.User_Detail_Class_Time_Data(
                            rs.getDate("日期"),
                            rs.getString("星期幾"),
                            rs.getString("開始時間"),
                            rs.getString("結束時間"),
                            rs.getInt("預約人數"),
                            rs.getInt("上課人數"),
                            rs.getInt("課表編號")
                    ));
                rs.close();
                preparedStatement.close();
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
            }
            new Handler(Looper.getMainLooper()).post(this::updateUI);
        });
    }
    private void updateUI() {
        if (getActivity() != null && isAdded()) {
            User_Detail_Class_Time_Fragment.DetailClassTimeAdapter detailClassTimeAdapter  = new User_Detail_Class_Time_Fragment.DetailClassTimeAdapter(getActivity(),detailClassTimeData,binding.getRoot());
            RecyclerView detailclasstimeRecyclerView = binding.userDetailClassTimeRecyclerView;
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            detailclasstimeRecyclerView.setLayoutManager(layoutManager);
            detailclasstimeRecyclerView.setAdapter(detailClassTimeAdapter);

        }
    }
    public List<java.sql.Date> getDate(int classId) {
        List<java.sql.Date> courseDates = new ArrayList<>();

        try {
            MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
            String query = "SELECT DISTINCT 日期 FROM 健身教練課表課程合併 WHERE 課程編號 = ?";
            PreparedStatement preparedStatement = MyConnection.prepareStatement(query);
            preparedStatement.setInt(1, classId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                java.sql.Date date = resultSet.getDate("日期");
                courseDates.add(date);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courseDates;
    }
    static class User_Detail_Class_Time_Data {
        Date date;
        String week,sttime,edtime;
        int appeople,classpeople,scheduleID;
        static ArrayList<User_Detail_Class_Time_Data> detailClassTimeData = new ArrayList<>();

        public User_Detail_Class_Time_Data(Date date,String week,String sttime,String edtime,int appeople,int classpeople,int scheduleID) {
            this.date = date;
            this.week=week;
            this.sttime=sttime;
            this.edtime=edtime;
            this.appeople=appeople;
            this.classpeople=classpeople;
            this.scheduleID=scheduleID;
        }
        public static ArrayList<User_Detail_Class_Time_Data> getdata() {
            if (detailClassTimeData == null) {
                return null;
            }
            return detailClassTimeData;
        }

        private Date getDate() {
            return date;
        }
        private String getWeek(){return week;}
        private String getSttime(){return sttime;}
        private String getEdtime(){return edtime;}
        private int getAppeople(){return appeople;}
        private int getClasspeople(){return classpeople;}
        private int getScheduleID(){return scheduleID;}

    }
    class DetailClassTimeAdapter extends RecyclerView.Adapter<User_Detail_Class_Time_Fragment.DetailClassTimeAdapter.ViewHolder>
    {

        List<User_Detail_Class_Time_Fragment.User_Detail_Class_Time_Data> detailClassTimeDataList;
        Context context;
        View view;
        public DetailClassTimeAdapter(Context context, List<User_Detail_Class_Time_Fragment.User_Detail_Class_Time_Data> detailClassTimeDataList, View view) {
            this.context = context;
            this.detailClassTimeDataList = detailClassTimeDataList;
            this.view=view;
        }
        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView detaildate,detailweek,detailtime,detailpeople;
            Button to_ap_btn;
            public ViewHolder(View itemView) {
                super(itemView);
                detaildate=itemView.findViewById(R.id.user_detail_class_date);
                detailweek=itemView.findViewById(R.id.user_detail_class_week);
                detailtime=itemView.findViewById(R.id.user_detail_class_time);
                detailpeople=itemView.findViewById(R.id.user_detail_class_people);
                to_ap_btn=itemView.findViewById(R.id.user_detail_class_apbtn);
            }
        }

        @NonNull
        @Override
        public User_Detail_Class_Time_Fragment.DetailClassTimeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_detail_class_time_fragment_item, parent, false);
            return new User_Detail_Class_Time_Fragment.DetailClassTimeAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull User_Detail_Class_Time_Fragment.DetailClassTimeAdapter.ViewHolder holder, int position) {
            User_Detail_Class_Time_Fragment.User_Detail_Class_Time_Data item = detailClassTimeDataList.get(position);

            holder.detaildate.setText(item.getDate().toString());
            holder.detailweek.setText(item.getWeek());
            holder.detailtime.setText(item.getSttime()+"~"+item.getEdtime());
            holder.detailpeople.setText("目前人數："+item.getAppeople()+"/"+item.getClasspeople());
            try {
                MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
                String query = "SELECT 日期,預約人數,上課人數,開始時間 FROM 健身教練課表課程合併 WHERE 課程編號 = ? ORDER BY [開始時間]";
                PreparedStatement Statement = MyConnection.prepareStatement(query);
                Statement.setInt(1, classID);

                ResultSet rs = Statement.executeQuery();
                java.sql.Time currentTime = new java.sql.Time(System.currentTimeMillis());

                while (rs.next()) {
                    java.sql.Time startTime = java.sql.Time.valueOf(rs.getString("開始時間") + ":00");

                    if (item.getAppeople()>=item.getClasspeople()) {
                        holder.to_ap_btn.setText("額滿");
                        holder.to_ap_btn.setEnabled(false);
                        continue;
                    }
                    String systemDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    if(systemDate.equals(item.getDate().toString())){
                        if (startTime.before(currentTime)) {
                            holder.to_ap_btn.setText("已逾時");
                            holder.to_ap_btn.setEnabled(false);
                            continue;
                        }
                        continue;
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            holder.to_ap_btn.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), User_Confirm_Appointment.class);
                intent.putExtra("預約課表ID", item.getScheduleID());
                startActivity(intent);
            });


        }
        @Override
        public int getItemCount() {
            return detailClassTimeDataList.size();
        }
    }

}
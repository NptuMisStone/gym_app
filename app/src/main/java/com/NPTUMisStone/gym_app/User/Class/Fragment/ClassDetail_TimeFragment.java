package com.NPTUMisStone.gym_app.User.Class.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;

import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Records.Confirm;
import com.NPTUMisStone.gym_app.databinding.UserClassDetailTimeFragmentBinding;
import com.hdev.calendar.bean.DateInfo;
import com.hdev.calendar.view.SingleCalendarView;

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
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

public class ClassDetail_TimeFragment extends Fragment {

    private UserClassDetailTimeFragmentBinding binding;
    Connection MyConnection;
    private int classID;
    private List<Date> courseDates;
    SingleCalendarView singleCalendarView;
    ArrayList<ClassDetail_TimeFragment.User_Detail_Class_Time_Data> detailClassTimeData = new ArrayList<>();

    public static ClassDetail_TimeFragment newInstance() {
        return new ClassDetail_TimeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding=UserClassDetailTimeFragmentBinding.inflate(inflater,container,false);


        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        classID = sharedPreferences.getInt("看更多課程ID", 0);
        courseDates =getDate(classID);

        setupCalendar();


        return binding.getRoot();

    }
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        classID = sharedPreferences.getInt("看更多課程ID", 0);
        courseDates =getDate(classID);
        setupCalendar();
    }
    private void setupCalendar() {
        if (courseDates == null || courseDates.isEmpty()) {
            courseDates = getDate(classID);
        }
        if (courseDates.isEmpty()) {
            Log.e("CourseDates", "沒有課程日期!");
            return;
        }
        singleCalendarView = binding.userDetailClassChooseDate;
        DateInfo startDate= new DateInfo();
        Calendar startcalendar = Calendar.getInstance();
        startDate.setYear(startcalendar.get(Calendar.YEAR));
        startDate.setMonth(startcalendar.get(Calendar.MONTH) + 1); // 月份從0開始，故+1
        startDate.setDay(startcalendar.get(Calendar.DAY_OF_MONTH));

        // 設置結束日期為courseDates中最大的日期(那個課程的最大日期)
        Date maxDate = Collections.max(courseDates);
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

        // 今日之後可點擊日期
        List<DateInfo> clickableDates = new ArrayList<>();
        Date today = new Date(System.currentTimeMillis());
        for (Date date : courseDates) {
            if (!date.before(today)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                clickableDates.add(new DateInfo(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH)
                ));
            }
        }
        // 今日有課可點擊
        Calendar calendar2 = Calendar.getInstance();
        DateInfo todayDateInfo = new DateInfo(
                calendar2.get(Calendar.YEAR),
                calendar2.get(Calendar.MONTH) + 1,
                calendar2.get(Calendar.DAY_OF_MONTH)
        );
        if (!clickableDates.contains(todayDateInfo)) {
            clickableDates.add(todayDateInfo);
        }
        // 設置可點擊日期列表
        singleCalendarView.setClickableDateList(clickableDates);
        singleCalendarView.setOnSingleDateSelectedListener((calendar, date) -> {
            fetchTimeInfo(date);
            return null;
        });
        singleCalendarView.setSelectedDate(startDate);
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
                    detailClassTimeData.add(new ClassDetail_TimeFragment.User_Detail_Class_Time_Data(
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
            ClassDetail_TimeFragment.DetailClassTimeAdapter detailClassTimeAdapter  = new ClassDetail_TimeFragment.DetailClassTimeAdapter(getActivity(),detailClassTimeData,binding.getRoot());
            RecyclerView detailclasstimeRecyclerView = binding.userDetailClassTimeRecyclerView;
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            detailclasstimeRecyclerView.setLayoutManager(layoutManager);
            detailclasstimeRecyclerView.setAdapter(detailClassTimeAdapter);

        }
    }
    public List<Date> getDate(int classId) {
        List<Date> courseDates = new ArrayList<>();

        try {
            MyConnection = new SQLConnection(binding.getRoot()).IWantToConnection();
            String query = "SELECT DISTINCT 日期 FROM 健身教練課表課程合併 WHERE 課程編號 = ?";
            PreparedStatement preparedStatement = MyConnection.prepareStatement(query);
            preparedStatement.setInt(1, classId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Date date = resultSet.getDate("日期");
                courseDates.add(date);
            }
        } catch (SQLException e) {
            Log.e("SQL", Objects.requireNonNull(e.getMessage()));
        }
        return courseDates;
    }
    static class User_Detail_Class_Time_Data {
        Date date;
        String week,sttime,edtime;
        int appeople,classpeople,scheduleID;

        public User_Detail_Class_Time_Data(Date date,String week,String sttime,String edtime,int appeople,int classpeople,int scheduleID) {
            this.date = date;
            this.week=week;
            this.sttime=sttime;
            this.edtime=edtime;
            this.appeople=appeople;
            this.classpeople=classpeople;
            this.scheduleID=scheduleID;
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
    class DetailClassTimeAdapter extends RecyclerView.Adapter<ClassDetail_TimeFragment.DetailClassTimeAdapter.ViewHolder>
    {

        List<ClassDetail_TimeFragment.User_Detail_Class_Time_Data> detailClassTimeDataList;
        Context context;
        View view;
        public DetailClassTimeAdapter(Context context, List<ClassDetail_TimeFragment.User_Detail_Class_Time_Data> detailClassTimeDataList, View view) {
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
        public ClassDetail_TimeFragment.DetailClassTimeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_class_detail_time_fragment_item, parent, false);
            return new ClassDetail_TimeFragment.DetailClassTimeAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ClassDetail_TimeFragment.DetailClassTimeAdapter.ViewHolder holder, int position) {
            ClassDetail_TimeFragment.User_Detail_Class_Time_Data item = detailClassTimeDataList.get(position);

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
                Time currentTime = new Time(System.currentTimeMillis());

                while (rs.next()) {
                    Time startTime = Time.valueOf(rs.getString("開始時間") + ":00");

                    if (item.getAppeople()>=item.getClasspeople()) {
                        holder.to_ap_btn.setText("額滿");
                        holder.to_ap_btn.setEnabled(false);
                        continue;
                    }
                    String systemDate = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN).format(new Date());
                    if(systemDate.equals(item.getDate().toString())){
                        if (startTime.before(currentTime)) {
                            holder.to_ap_btn.setText("已逾時");
                            holder.to_ap_btn.setEnabled(false);
                        }
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                Log.e("SQL", Objects.requireNonNull(e.getMessage()));
            }
            holder.to_ap_btn.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), Confirm.class);
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
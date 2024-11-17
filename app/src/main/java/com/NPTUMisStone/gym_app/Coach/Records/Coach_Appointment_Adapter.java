package com.NPTUMisStone.gym_app.Coach.Records;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.Coach.Main.Coach;
import com.NPTUMisStone.gym_app.Main.Initial.SQLConnection;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Main.User;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;

public class Coach_Appointment_Adapter extends RecyclerView.Adapter<Coach_Appointment_Adapter.ViewHolder> {
    List<Coach_AppointmentData> appointmentDataList;
    Context context;
    Connection MyConnection;
    public Coach_Appointment_Adapter(Context context, List<Coach_AppointmentData> appointmentList) {
        this.context = context;
        this.appointmentDataList = appointmentList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ap_date,ap_week,ap_className,ap_time,ap_place,ap_people;
        Button detailBtn;
        LinearLayout placetype;
        public ViewHolder(View itemView) {
            super(itemView);
            ap_date = itemView.findViewById(R.id.coach_appointment_date);
            ap_week = itemView.findViewById(R.id.coach_appointment_week);
            ap_className=itemView.findViewById(R.id.coach_appointment_class_name);
            ap_time=itemView.findViewById(R.id.coach_appointment_time);
            ap_place=itemView.findViewById(R.id.coach_appointment_place);
            ap_people=itemView.findViewById(R.id.coach_appointment_people);
            detailBtn=itemView.findViewById(R.id.coach_appointment_detailBtn);
            placetype=itemView.findViewById(R.id.coach_appointment_placetype);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.coach_appointment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Coach_AppointmentData item = appointmentDataList.get(position);
        Log.i("Adapter", "正在綁定數據: " + item.getClassName());
        holder.ap_date.setText(item.getDate().toString());
        holder.ap_week.setText(item.getWeek());
        holder.ap_className.setText(item.getClassName());
        holder.ap_time.setText(item.getStTime().trim()+" ~ "+item.getEdTime().trim());
        holder.ap_place.setText(item.getPlace());
        holder.ap_people.setText(String.valueOf(item.getAppeople()).trim() +" / " + String.valueOf(item.getClasspeople()).trim());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                MyConnection = new SQLConnection(holder.itemView).IWantToConnection();
                String sql = "Select 地點類型 From 健身教練課表課程合併 Where 健身教練編號= ? AND 課表編號 = ? ";
                PreparedStatement Statement = MyConnection.prepareStatement(sql);
                Statement.setInt(1, Coach.getInstance().getCoachId());
                Statement.setInt(2, item.getScheduleID());
                ResultSet rs = Statement.executeQuery();
                if (rs.next()) {
                    if (rs.getString("地點類型").trim().equals("2")) {
                        holder.placetype.setBackgroundResource(R.drawable.course_time_blue);
                    } else {
                        holder.placetype.setBackgroundResource(R.drawable.course_time_red);
                    }
                }
                rs.close();
                Statement.close();
            } catch (SQLException e) {
                Log.e("SQL", "類型", e);
            }
        });
    }
    @Override
    public int getItemCount() {
        return appointmentDataList.size();
    }
}


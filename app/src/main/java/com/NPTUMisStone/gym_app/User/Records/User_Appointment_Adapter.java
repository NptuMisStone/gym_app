package com.NPTUMisStone.gym_app.User.Records;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.ImageHandle;

import java.util.List;

public class User_Appointment_Adapter extends RecyclerView.Adapter<User_Appointment_Adapter.ViewHolder> {
    List<User_AppointmentData> appointmentDataList;
    Context context;

    public User_Appointment_Adapter(Context context, List<User_AppointmentData> appointmentList) {
        this.context = context;
        this.appointmentDataList = appointmentList;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_appointment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User_AppointmentData item = appointmentDataList.get(position);
        Log.i("Adapter", "正在綁定數據: " + item.getClassName());

        if (item.getCoachimage() != null) {
            Bitmap bitmap = ImageHandle.getBitmap(item.getCoachimage());
            holder.coach_image.setImageBitmap(ImageHandle.resizeBitmap(bitmap));
        }
        holder.ap_date.setText(item.getDate().toString());
        holder.ap_week.setText(item.getWeek());
        holder.ap_Time.setText(item.getTime());
        holder.class_time_long.setText(String.valueOf(item.getTimeLong()));
        holder.class_name.setText(item.getClassName());
        holder.class_price.setText(item.getClassPrice());
        holder.coach_name.setText(item.getCoachName());
        holder.note.setText(item.getNote() != null ? item.getNote() : "");
    }


    @Override
    public int getItemCount() {
        return appointmentDataList.size();
    }
}


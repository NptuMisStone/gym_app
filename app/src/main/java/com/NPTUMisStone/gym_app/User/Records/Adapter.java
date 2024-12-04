package com.NPTUMisStone.gym_app.User.Records;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User_And_Coach.Helper.ImageHandle;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    List<Data> appointmentDataList;
    Context context;

    public Adapter(Context context, List<Data> appointmentList) {
        this.context = context;
        this.appointmentDataList = appointmentList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coach_image;
        TextView ap_date, ap_Time, class_time_long, class_name, class_price, coach_name, note, ap_week;
        View status;
        LinearLayout statusarea;
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
            status=itemView.findViewById(R.id.statusColor);
            statusarea=itemView.findViewById(R.id.StatusArea);
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
        Data item = appointmentDataList.get(position);
        Log.i("Adapter", "正在綁定數據: " + item.getClassName());

        int statusColor = switch (item.getStatus()) {
            case 3 -> ContextCompat.getColor(context, R.color.cancel_ap);
            case 4 -> ContextCompat.getColor(context, R.color.past_ap);
            case 5 -> ContextCompat.getColor(context, R.color.coach_cancel_ap);
            default -> ContextCompat.getColor(context, R.color.black);
        };
        holder.status.setBackgroundColor(statusColor);
        holder.statusarea.setBackgroundColor(statusColor);

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


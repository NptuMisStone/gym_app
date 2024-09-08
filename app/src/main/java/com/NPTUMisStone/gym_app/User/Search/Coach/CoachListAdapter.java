package com.NPTUMisStone.gym_app.User.Search.Coach;

import android.content.Context;
import android.content.Intent;
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

public class CoachListAdapter extends RecyclerView.Adapter<CoachListAdapter.ViewHolder> {
    List<CoachListData> coachList;
    Context context;

    public CoachListAdapter(Context context, List<CoachListData> coachList) {
        this.context = context;
        this.coachList = coachList; // Direct assignment, assuming the list is not modified elsewhere
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView_head;
        TextView textView_name;
        TextView textView_address;
        TextView textView_rating;

        private ViewHolder(View itemView) {
            super(itemView);
            imageView_head = itemView.findViewById(R.id.imv_coachHead);
            textView_name = itemView.findViewById(R.id.name_text_view);
            textView_address = itemView.findViewById(R.id.address_text_view);
            textView_rating = itemView.findViewById(R.id.textView_rating);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_search_coach_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CoachListData item = coachList.get(position);
        if (item.getCoachHead() != null)
            holder.imageView_head.setImageBitmap(ImageHandle.getBitmap(item.getCoachHead()));
        holder.textView_name.setText(item.getCoachName());
        holder.textView_address.setText(item.getCoachDescription());
        holder.itemView.setTag(item);
        holder.itemView.setOnClickListener(view -> {
            if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, CoachDetail.class);
                intent.putExtra("coach_id", item.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coachList.size();
    }
}
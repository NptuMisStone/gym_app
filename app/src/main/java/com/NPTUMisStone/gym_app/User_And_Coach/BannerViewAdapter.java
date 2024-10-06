package com.NPTUMisStone.gym_app.User_And_Coach;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BannerViewAdapter extends RecyclerView.Adapter<BannerViewAdapter.ViewHolder> {

    List<Advertisement> adList;
    Context context;

    public BannerViewAdapter(List<Advertisement> adList, Context context) {
        this.adList = adList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Ensure the image fills the entire space
        imageView.setId(View.generateViewId());
        return new ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Advertisement ad = adList.get(position);
        Bitmap bitmap = BitmapFactory.decodeByteArray(ad.getImage(), 0, ad.getImage().length);
        holder.imageView.setImageBitmap(bitmap);
        holder.imageView.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ad.getLink()));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return adList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}

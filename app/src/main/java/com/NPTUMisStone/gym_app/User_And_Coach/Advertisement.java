package com.NPTUMisStone.gym_app.User_And_Coach;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class Advertisement {
    byte[] image;
    String link;
    Date startDate;
    Date endDate;
    public Advertisement(byte[] image, String link, Date startDate, Date endDate) {
        this.image = image;
        this.link = link;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    public byte[] getImage() { return image;    }
    public String getLink() { return link; }
    public Date getStartDate() { return startDate;}
    public Date getEndDate() { return endDate; }

    public static void init_banner(Context context, Connection connection, List<Advertisement> adList, ViewPager2 viewPager, SwipeRefreshLayout swipeRefreshLayout) {
        viewPager.setAdapter(new BannerViewAdapter(adList, context));
        loadAdvertisements(connection, adList, viewPager);
        startAutoSlide(viewPager, adList);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadAdvertisements(connection, adList, viewPager);
                swipeRefreshLayout.setRefreshing(false);
            });
        }
    }

    public static void loadAdvertisements(Connection connection, List<Advertisement> adList, ViewPager2 viewPager) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String searchQuery = "SELECT 圖片, 連結, 上架日, 下架日 FROM 廣告 WHERE GETDATE() BETWEEN 上架日 AND 下架日";
                ResultSet resultSet = connection.prepareStatement(searchQuery).executeQuery();
                List<Advertisement> newAdList = new ArrayList<>();
                while (resultSet.next()) {
                    byte[] image = resultSet.getBytes("圖片");
                    String link = resultSet.getString("連結");
                    Date startDate = resultSet.getDate("上架日");
                    Date endDate = resultSet.getDate("下架日");
                    newAdList.add(new Advertisement(image, link, startDate, endDate));
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    adList.clear();
                    adList.addAll(newAdList);
                    Objects.requireNonNull(viewPager.getAdapter()).notifyItemRangeChanged(0, adList.size());
                });
            } catch (Exception e) {
                Log.e("SQL", "Error in SQL", e);
            }
        });
    }

    private static void startAutoSlide(ViewPager2 viewPager, List<Advertisement> adList) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!adList.isEmpty()) {
                    viewPager.setCurrentItem((viewPager.getCurrentItem() + 1) % adList.size());
                    new Handler(Looper.getMainLooper()).postDelayed(this, 6000);
                } else {
                    Log.e("AutoSlide", "Advertisement list is empty, cannot slide");
                }
            }
        }, 6000);
    }
}


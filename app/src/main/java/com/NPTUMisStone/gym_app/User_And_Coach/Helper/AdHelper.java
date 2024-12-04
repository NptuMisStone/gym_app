package com.NPTUMisStone.gym_app.User_And_Coach.Helper;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

public class AdHelper {
    //Google AdMob：https://developers.google.com/admob/android/quick-start?hl=zh-TW
    //AdView：https://developers.google.com/android/reference/com/google/android/gms/ads/AdView
    //下一步：https://thumbb13555.pixnet.net/blog/post/333622882
    public static void initializeAndLoadAd(Context context, int adViewId) {
        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(context, initializationStatus -> {});
        // Ensure AdView creation and loadAd call are on the main UI thread
        ((AppCompatActivity) context).runOnUiThread(() -> {
            // Find the AdView by its ID
            AdView adView = ((AppCompatActivity) context).findViewById(adViewId);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdSwipeGestureClicked() {
                    super.onAdSwipeGestureClicked();
                }
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                }
                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                }
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                }
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                }
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                }
            });
            // Create an ad request.
            AdRequest adRequest = new AdRequest.Builder().build();
            // Start loading the ad.
            adView.loadAd(adRequest);
        });
    }
}
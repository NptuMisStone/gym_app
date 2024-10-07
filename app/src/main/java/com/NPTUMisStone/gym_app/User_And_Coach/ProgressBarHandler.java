package com.NPTUMisStone.gym_app.User_And_Coach;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class ProgressBarHandler {
    Activity activity;
    View darkBackground;
    ProgressBar progressBar;
    private boolean isLoading = false;

    public ProgressBarHandler(Activity activity, FrameLayout parentLayout) {
        this.activity = activity;
        darkBackground = new View(activity);
        darkBackground.setBackgroundColor(0x88000000); // Semi-transparent black
        darkBackground.setVisibility(View.GONE);

        progressBar = new ProgressBar(activity);
        progressBar.setBackgroundColor(0xFFFFFFFF); // White

        FrameLayout.LayoutParams progressBarParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        progressBarParams.gravity = android.view.Gravity.CENTER;

        FrameLayout overlayLayout = new FrameLayout(activity);
        overlayLayout.addView(progressBar, progressBarParams);
        overlayLayout.addView(darkBackground, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        parentLayout.addView(overlayLayout);
    }

    public void showProgressBar() {
        activity.runOnUiThread(() -> {
            isLoading = true;
            darkBackground.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    public void hideProgressBar() {
        activity.runOnUiThread(() -> {
            isLoading = false;
            darkBackground.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        });
    }

    public boolean isLoading() {
        return isLoading;
    }
}
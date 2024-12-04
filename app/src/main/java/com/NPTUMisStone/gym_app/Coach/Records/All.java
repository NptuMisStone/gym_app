package com.NPTUMisStone.gym_app.Coach.Records;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.NPTUMisStone.gym_app.Coach.Records.Fragment.FutureFragment;
import com.NPTUMisStone.gym_app.Coach.Records.Fragment.PastFragment;
import com.NPTUMisStone.gym_app.Coach.Records.Fragment.TodayFragment;
import com.NPTUMisStone.gym_app.R;
import com.google.android.material.tabs.TabLayout;

public class All extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_appointment_all);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 返回按鈕
        findViewById(R.id.coach_Appointment_back).setOnClickListener(v -> finish());
        getSupportFragmentManager().beginTransaction().replace(R.id.CoachAppointmentFrameLayout,new TodayFragment()).addToBackStack(null).commit();
        ((TabLayout)findViewById(R.id.CoachAppointmentTabLayout)).addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = switch (tab.getPosition()) {
                    case 0 -> new TodayFragment();
                    case 1 -> new FutureFragment();
                    case 2 -> new PastFragment();
                    default -> null;
                };
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.CoachAppointmentFrameLayout, fragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                } else {
                    Log.e("AppointmentAll", "無法載入 Fragment");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

    }
}
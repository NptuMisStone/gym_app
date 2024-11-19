package com.NPTUMisStone.gym_app.Coach.Records;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.NPTUMisStone.gym_app.Coach.Records.CoachFutureAppointment.CoachFutureAppointment;
import com.NPTUMisStone.gym_app.Coach.Records.CoachPastAppointment.CoachPastAppointment;
import com.NPTUMisStone.gym_app.Coach.Records.CoachTodayAppointment.CoachTodayAppointment;
import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Records.CancelAppointment.CancelAppointmentFragment;
import com.NPTUMisStone.gym_app.User.Records.CoachCancelAppointment.CoachCancelAppointmentFragment;
import com.NPTUMisStone.gym_app.User.Records.FinishAppointment.FinishAppointmentFragment;
import com.NPTUMisStone.gym_app.User.Records.NowAppointment.NowAppointmentFragment;
import com.NPTUMisStone.gym_app.User.Records.PastAppointment.PastAppointmentFragment;
import com.google.android.material.tabs.TabLayout;

public class Coach_AppointmentsAll extends AppCompatActivity {
    FrameLayout frameLayout;
    TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.coach_appointments_all);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 返回按鈕
        findViewById(R.id.coach_Appointment_back).setOnClickListener(v -> finish());

        frameLayout=(FrameLayout)findViewById(R.id.CoachAppointmentFrameLayout);
        tabLayout=(TabLayout)findViewById(R.id.CoachAppointmentTabLayout);

        getSupportFragmentManager().beginTransaction().replace(R.id.CoachAppointmentFrameLayout,new CoachTodayAppointment()).addToBackStack(null).commit();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new CoachTodayAppointment();
                        break;
                    case 1:
                        fragment = new CoachFutureAppointment();
                        break;
                    case 2:
                        fragment = new CoachPastAppointment();
                        break;
                }
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
    public  void coach_Appointment_goback(View view){
        finish();
    }
}
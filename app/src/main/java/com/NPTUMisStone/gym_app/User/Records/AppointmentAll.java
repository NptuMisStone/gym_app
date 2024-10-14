package com.NPTUMisStone.gym_app.User.Records;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Main.UserHome;
import com.NPTUMisStone.gym_app.User.Records.CancelAppointment.CancelAppointmentFragment;
import com.NPTUMisStone.gym_app.User.Records.CoachCancelAppointment.CoachCancelAppointmentFragment;
import com.NPTUMisStone.gym_app.User.Records.FinishAppointment.FinishAppointmentFragment;
import com.NPTUMisStone.gym_app.User.Records.NowAppointment.NowAppointmentFragment;
import com.NPTUMisStone.gym_app.User.Records.PastAppointment.PastAppointmentFragment;
import com.google.android.material.tabs.TabLayout;

public class AppointmentAll extends AppCompatActivity {

    FrameLayout frameLayout;
    TabLayout tabLayout;
    ImageButton Gobackbtn;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_appointment_all);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Gobackbtn=(ImageButton)findViewById(R.id.user_Appointment_back);
        frameLayout=(FrameLayout)findViewById(R.id.AppointmentFrameLayout);
        tabLayout=(TabLayout)findViewById(R.id.AppointmentTabLayout);
        getSupportFragmentManager().beginTransaction().replace(R.id.AppointmentFrameLayout,new NowAppointmentFragment()).addToBackStack(null).commit();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new NowAppointmentFragment();
                        break;
                    case 1:
                        fragment = new FinishAppointmentFragment();
                        break;
                    case 2:
                        fragment = new CancelAppointmentFragment();
                        break;
                    case 3:
                        fragment = new PastAppointmentFragment();
                        break;
                    case 4:
                        fragment = new CoachCancelAppointmentFragment();
                        break;
                }
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.AppointmentFrameLayout, fragment)
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
    public  void user_Appointment_goback(View view){
        Intent intent=new Intent();
        intent.setClass(this, UserHome.class);
        startActivity(intent);
        finish();
    }
}
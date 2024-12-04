package com.NPTUMisStone.gym_app.User.Records;

import android.annotation.SuppressLint;
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

import com.NPTUMisStone.gym_app.R;
import com.NPTUMisStone.gym_app.User.Records.Fragment.CancelFragment;
import com.NPTUMisStone.gym_app.User.Records.Fragment.CoachCancelFragment;
import com.NPTUMisStone.gym_app.User.Records.Fragment.FinishFragment;
import com.NPTUMisStone.gym_app.User.Records.Fragment.NowFragment;
import com.NPTUMisStone.gym_app.User.Records.Fragment.PastFragment;
import com.google.android.material.tabs.TabLayout;

public class Appointment extends AppCompatActivity {

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
        int index =getIntent().getIntExtra("是否是評論",0);
        if (index == 1) {
            // 從評論回來
            tabLayout.selectTab(tabLayout.getTabAt(1));
            getSupportFragmentManager().beginTransaction().replace(R.id.AppointmentFrameLayout,new FinishFragment()).addToBackStack(null).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.AppointmentFrameLayout,new NowFragment()).addToBackStack(null).commit();
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new NowFragment();
                        break;
                    case 1:
                        fragment = new FinishFragment();
                        break;
                    case 2:
                        fragment = new CancelFragment();
                        break;
                    case 3:
                        fragment = new PastFragment();
                        break;
                    case 4:
                        fragment = new CoachCancelFragment();
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
        finish();
    }
}
package com.NPTUMisStone.gym_app.User.Like;

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
import com.NPTUMisStone.gym_app.User.Like.Fragment.UserLike_ClassFragment;
import com.NPTUMisStone.gym_app.User.Like.Fragment.UserLike_CoachFragment;
import com.google.android.material.tabs.TabLayout;

public class UserLike extends AppCompatActivity {
    FrameLayout frameLayout;
    TabLayout tabLayout;
    ImageButton Gobackbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_like);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Gobackbtn=(ImageButton)findViewById(R.id.user_Like_back);
        frameLayout=(FrameLayout)findViewById(R.id.LikeFrameLayout);
        tabLayout=(TabLayout)findViewById(R.id.LikeTabLayout);
        getSupportFragmentManager().beginTransaction().replace(R.id.LikeFrameLayout,new UserLike_CoachFragment()).addToBackStack(null).commit();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new UserLike_CoachFragment();
                        break;
                    case 1:
                        fragment = new UserLike_ClassFragment();
                        break;
                }
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.LikeFrameLayout, fragment)
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
    public void user_Like_goback(View view){
        finish();
    }
}
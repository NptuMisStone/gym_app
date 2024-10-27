package com.NPTUMisStone.gym_app.User.AllClass.DetailClass;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.NPTUMisStone.gym_app.R;

public class User_Detail_Class_Time_Fragment extends Fragment {

    private UserDetailClassTimeViewModel mViewModel;

    public static User_Detail_Class_Time_Fragment newInstance() {
        return new User_Detail_Class_Time_Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_detail_class_time_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UserDetailClassTimeViewModel.class);
        // TODO: Use the ViewModel
    }

}
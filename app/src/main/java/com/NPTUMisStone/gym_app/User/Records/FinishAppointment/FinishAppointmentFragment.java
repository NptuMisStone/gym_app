package com.NPTUMisStone.gym_app.User.Records.FinishAppointment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.NPTUMisStone.gym_app.R;

public class FinishAppointmentFragment extends Fragment {

    private FinishAppointmentViewModel mViewModel;

    public static FinishAppointmentFragment newInstance() {
        return new FinishAppointmentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_fragment_finish_appointment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(FinishAppointmentViewModel.class);
        // TODO: Use the ViewModel
    }

}
package com.NPTUMisStone.gym_app.User.Records.CoachCancelAppointment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.NPTUMisStone.gym_app.R;

public class CoachCancelAppointmentFragment extends Fragment {

    private CoachCancelAppointmentViewModel mViewModel;

    public static CoachCancelAppointmentFragment newInstance() {
        return new CoachCancelAppointmentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_fragment_coach_cancel_appointment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CoachCancelAppointmentViewModel.class);
        // TODO: Use the ViewModel
    }

}
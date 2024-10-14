package com.NPTUMisStone.gym_app.User.Records.PastAppointment;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.NPTUMisStone.gym_app.R;

public class PastAppointmentFragment extends Fragment {

    private PastAppointmentViewModel mViewModel;

    public static PastAppointmentFragment newInstance() {
        return new PastAppointmentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_fragment_past_appointment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(PastAppointmentViewModel.class);
        // TODO: Use the ViewModel
    }

}
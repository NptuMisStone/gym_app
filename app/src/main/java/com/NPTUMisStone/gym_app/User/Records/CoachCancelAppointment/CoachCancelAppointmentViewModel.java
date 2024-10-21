package com.NPTUMisStone.gym_app.User.Records.CoachCancelAppointment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CoachCancelAppointmentViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public CoachCancelAppointmentViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is CoachCancelAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
package com.NPTUMisStone.gym_app.User.Records;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Appointment_CoachCancelViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public Appointment_CoachCancelViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is CoachCancelAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
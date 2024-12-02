package com.NPTUMisStone.gym_app.User.Records;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Appointment_NowViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public Appointment_NowViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is NowAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
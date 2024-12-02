package com.NPTUMisStone.gym_app.User.Records;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Appointment_FinishViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public Appointment_FinishViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is FinishAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
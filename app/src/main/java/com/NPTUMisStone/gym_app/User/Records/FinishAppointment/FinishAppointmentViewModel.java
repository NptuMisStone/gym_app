package com.NPTUMisStone.gym_app.User.Records.FinishAppointment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FinishAppointmentViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public FinishAppointmentViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is FinishAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
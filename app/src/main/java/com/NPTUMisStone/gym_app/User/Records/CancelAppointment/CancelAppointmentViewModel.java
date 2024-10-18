package com.NPTUMisStone.gym_app.User.Records.CancelAppointment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CancelAppointmentViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public CancelAppointmentViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is CancelAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
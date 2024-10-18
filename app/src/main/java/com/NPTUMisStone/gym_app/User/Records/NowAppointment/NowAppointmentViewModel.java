package com.NPTUMisStone.gym_app.User.Records.NowAppointment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NowAppointmentViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public NowAppointmentViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is NowAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
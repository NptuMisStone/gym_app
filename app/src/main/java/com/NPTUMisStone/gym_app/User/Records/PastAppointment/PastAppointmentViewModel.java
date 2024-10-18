package com.NPTUMisStone.gym_app.User.Records.PastAppointment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PastAppointmentViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public PastAppointmentViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is PastAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
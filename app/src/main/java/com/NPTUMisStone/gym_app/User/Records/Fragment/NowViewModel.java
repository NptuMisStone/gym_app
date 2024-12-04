package com.NPTUMisStone.gym_app.User.Records.Fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NowViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public NowViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is NowAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
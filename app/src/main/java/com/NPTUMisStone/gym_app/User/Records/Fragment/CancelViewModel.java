package com.NPTUMisStone.gym_app.User.Records.Fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CancelViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public CancelViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is CancelAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
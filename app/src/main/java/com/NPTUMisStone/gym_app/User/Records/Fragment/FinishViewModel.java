package com.NPTUMisStone.gym_app.User.Records.Fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FinishViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public FinishViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is FinishAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
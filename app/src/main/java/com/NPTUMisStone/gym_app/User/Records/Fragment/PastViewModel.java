package com.NPTUMisStone.gym_app.User.Records.Fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PastViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public PastViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is PastAP fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
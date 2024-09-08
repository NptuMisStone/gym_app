package com.NPTUMisStone.gym_app.User.Search.Coach;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CoachViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CoachViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
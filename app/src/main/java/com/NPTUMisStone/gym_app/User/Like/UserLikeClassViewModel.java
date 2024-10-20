package com.NPTUMisStone.gym_app.User.Like;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserLikeClassViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public UserLikeClassViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is LikeClass fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
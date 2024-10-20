package com.NPTUMisStone.gym_app.User.Like;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserLikeCoachViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public UserLikeCoachViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is LikeCoach fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
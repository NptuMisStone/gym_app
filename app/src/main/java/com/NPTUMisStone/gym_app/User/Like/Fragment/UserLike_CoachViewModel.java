package com.NPTUMisStone.gym_app.User.Like.Fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserLike_CoachViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public UserLike_CoachViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is LikeCoach fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
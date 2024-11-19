package com.NPTUMisStone.gym_app.User.Like;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserLike_ClassViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    public UserLike_ClassViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is LikeClass fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
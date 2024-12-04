package com.NPTUMisStone.gym_app.User_And_Coach.Helper;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.TextView;

public class ErrorHints {
    public static void editHint(EditText editText, String message) {
        editText.requestFocus();    //設定提示錯誤訊息：https://stackoverflow.com/questions/59963141/setting-error-enabled-to-false-on-textinputlayout-not-removing-extra-space-in-an
        editText.selectAll();   //提示錯誤訊息問題：https://stackoverflow.com/questions/77635123/iserrorenabled-not-working-kotlin-android-studio
        editText.setError(message);
    }
    public static void textHint(TextView statusHint, String message) {
        statusHint.setText(message);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        statusHint.setVisibility(View.VISIBLE);
        statusHint.startAnimation(fadeIn);
    }
}

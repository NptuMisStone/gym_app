package com.NPTUMisStone.gym_app.Main.Identify;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.Objects;

/**
 * Created by bruce on 2017/10/4.
 */
//Android 驗證碼輸入框的實現：https://www.jianshu.com/p/3238a5afc21c
public class VerifyCodeEditText extends AppCompatEditText {

    private long lastTime = 0;

    public VerifyCodeEditText(Context context) {
        super(context);
    }

    public VerifyCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerifyCodeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        this.setSelection(Objects.requireNonNull(this.getText()).length());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime < 500) {
                lastTime = currentTime;
                return true;
            } else {
                lastTime = currentTime;
            }
        }
        return super.onTouchEvent(event);
    }
}
package com.NPTUMisStone.mapview;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/**
 * Created by _SOLID
 * Date:2016/5/9
 * Time:11:30
 */
//SnackBar：別用Toast了，來試試Snack bar：https://blog.csdn.net/g984160547/article/details/121269520
//一行代码搞定Snack bar：https://www.jianshu.com/p/f4ba05d7bbda
//比系统自带的更好用的SnackBar：https://www.jianshu.com/p/e3c82b98f151
public class SnackBarUtils {
    static int color_danger = 0xffa94442;
    static int color_success = 0xff3c763d;
    static int color_info = 0xff31708f;
    static int color_warning = 0xff8a6d3b;

    static int action_color = 0xffCDC5BF;

    Snackbar mSnackbar;

    private SnackBarUtils(Snackbar snackbar) {
        mSnackbar = snackbar;
    }

    public static SnackBarUtils makeShort(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        return new SnackBarUtils(snackbar);
    }

    public static SnackBarUtils makeLong(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        return new SnackBarUtils(snackbar);
    }

    private View getSnackBarLayout(Snackbar snackbar) {
        if (snackbar != null) {
            return snackbar.getView();
        }
        return null;

    }


    private void setSnackBarBackColor(int colorId) {
        View snackBarView = getSnackBarLayout(mSnackbar);
        if (snackBarView != null) {
            snackBarView.setBackgroundColor(colorId);
        }
    }

    public void info() {
        setSnackBarBackColor(color_info);
        show();
    }

    public void info(String actionText, View.OnClickListener listener) {
        setSnackBarBackColor(color_info);
        show(actionText, listener);
    }

    public void warning() {
        setSnackBarBackColor(color_warning);
        show();
    }

    public void warning(String actionText, View.OnClickListener listener) {
        setSnackBarBackColor(color_warning);
        show(actionText, listener);
    }

    public void danger() {
        setSnackBarBackColor(color_danger);
        show();
    }

    public void danger(String actionText, View.OnClickListener listener) {
        setSnackBarBackColor(color_danger);
        show(actionText, listener);
    }

    public void confirm() {
        setSnackBarBackColor(color_success);
        show();
    }

    public void confirm(String actionText, View.OnClickListener listener) {
        setSnackBarBackColor(color_success);
        show(actionText, listener);
    }

    public void show() {
        mSnackbar.show();
    }

    public void show(String actionText, View.OnClickListener listener) {
        mSnackbar.setActionTextColor(action_color);
        mSnackbar.setAction(actionText, listener).show();
    }
}
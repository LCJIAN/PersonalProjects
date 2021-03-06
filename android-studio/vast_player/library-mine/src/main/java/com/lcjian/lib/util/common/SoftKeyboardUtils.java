package com.lcjian.lib.util.common;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SoftKeyboardUtils {

    /**
     * 隐藏键盘
     *
     * @param context Activity
     */
    public static void hideSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = ((Activity) context).getWindow().getDecorView();
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /**
     * 显示键盘
     *
     * @param context Activity
     */
    public static void showSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()) {
            imm.showSoftInput(view, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}

package com.lcjian.lib.areader.ui.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import com.lcjian.lib.areader.R;

public class SlidingDialog extends Dialog {

    public SlidingDialog(Context context) {
        super(context, R.style.Theme_Sliding_Dialog);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.MATCH_PARENT;
            onWindowAttributesChanged(layoutParams);
            window.setGravity(Gravity.END);
            window.setAttributes(layoutParams);
        }
    }
}

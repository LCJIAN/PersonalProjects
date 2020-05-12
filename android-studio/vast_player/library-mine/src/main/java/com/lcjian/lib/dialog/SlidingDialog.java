package com.lcjian.lib.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.lcjian.lib.R;

public class SlidingDialog extends Dialog {

    public SlidingDialog(Context context) {
        super(context, R.style.Theme_Sliding_Dialog);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        onWindowAttributesChanged(layoutParams);
        getWindow().setGravity(Gravity.RIGHT);
        getWindow().setAttributes(layoutParams);
    }
}

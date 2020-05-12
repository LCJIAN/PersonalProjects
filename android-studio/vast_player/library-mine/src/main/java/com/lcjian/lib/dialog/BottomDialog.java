package com.lcjian.lib.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.lcjian.lib.R;

public class BottomDialog extends Dialog {

    public BottomDialog(Context context) {
        super(context, android.R.style.Theme_NoTitleBar_Fullscreen);
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.dimAmount = 0.5f;
        onWindowAttributesChanged(layoutParams);
        getWindow().setBackgroundDrawable(new ColorDrawable());
        getWindow().setWindowAnimations(R.style.dialogstyle);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setAttributes(layoutParams);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }
}
package com.org.firefighting.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.donkingliang.consecutivescroller.IConsecutiveScroller;

import java.util.ArrayList;
import java.util.List;

public class MyLinearLayout extends LinearLayout implements IConsecutiveScroller {

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public View getCurrentScrollerView() {
        return getChildAt(getChildCount() - 1);
    }

    @Override
    public List<View> getScrolledViews() {
        List<View> views = new ArrayList<>();
        views.add(getChildAt(getChildCount() - 1));
        return views;
    }
}

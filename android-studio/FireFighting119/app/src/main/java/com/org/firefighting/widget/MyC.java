package com.org.firefighting.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import timber.log.Timber;

public class MyC extends CoordinatorLayout {
    public MyC(@NonNull Context context) {
        super(context);
    }

    public MyC(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyC(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Timber.d("onTouchEvent");
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Timber.d("onInterceptTouchEvent");
        boolean dd = super.onInterceptTouchEvent(ev);
        Timber.d(String.valueOf(dd));
        return dd;
    }
}

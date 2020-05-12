package com.lcjian.lib.widget;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class MyViewPager extends ViewPager {

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthSpecMode != MeasureSpec.EXACTLY || heightSpecMode != MeasureSpec.EXACTLY) {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
            if (widthSpecMode != MeasureSpec.EXACTLY) {
                int width = 0;
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    int w = child.getMeasuredWidth();
                    if (w > width) {
                        width = w;
                    }
                }
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            }
            if (heightSpecMode != MeasureSpec.EXACTLY) {
                int height = 0;
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    int h = child.getMeasuredHeight();
                    if (h > height) {
                        height = h;
                    }
                }
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

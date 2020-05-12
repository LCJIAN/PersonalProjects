package com.lcjian.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ListView;

import com.lcjian.lib.R;

public class AdaptiveListView extends ListView {

    private int mMaxHeight;

    public AdaptiveListView(Context context) {
        this(context, null);
    }

    public AdaptiveListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdaptiveListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AdaptiveListView, defStyleAttr, 0);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.AdaptiveListView_adaptiveListViewMaxHeight, 0);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mMaxHeight != 0 && getMeasuredHeight() > mMaxHeight) {
            setMeasuredDimension(getMeasuredWidth(), mMaxHeight);
        }
    }

    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
        invalidate();
    }
}

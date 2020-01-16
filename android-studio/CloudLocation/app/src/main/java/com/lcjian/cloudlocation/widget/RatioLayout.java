package com.lcjian.cloudlocation.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.lcjian.cloudlocation.R;


public class RatioLayout extends RelativeLayout {

    private int mFixedSide;
    private float mRatio;

    public RatioLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RatioLayout, defStyle, 0);
        mRatio = a.getFloat(R.styleable.RatioLayout_ratio, 1);
        mFixedSide = a.getInt(R.styleable.RatioLayout_fixedSide, 0);
        a.recycle();
    }

    public RatioLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioLayout(Context context) {
        this(context, null);
    }

    @SuppressWarnings("unused")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // For simple implementation, or internal size is always 0.
        // We depend on the container to specify the layout size of
        // our view. We can't really know what it is since we will be
        // adding and removing different arbitrary views and do not
        // want the layout to change as this happens.
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

        // Children are just made to fill our space.
        int childWidthSize = getMeasuredWidth();
        int childHeightSize = getMeasuredHeight();
        // if(childWidthSize > childHeightSize) {
        // childWidthSize = childHeightSize;
        // }else {
        // childHeightSize = childWidthSize;
        // }
        if (mFixedSide == 0) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childWidthSize * mRatio), MeasureSpec.EXACTLY);
        } else if (mFixedSide == 1) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childHeightSize / mRatio), MeasureSpec.EXACTLY);
        } else {
            if (childWidthSize * mRatio > childHeightSize) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY);
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childHeightSize / mRatio), MeasureSpec.EXACTLY);
            } else {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childWidthSize * mRatio), MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setRatio(float ratio) {
        mRatio = ratio;
        requestLayout();
    }
}

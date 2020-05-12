package com.winside.lighting.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.viewpager.widget.ViewPager;


public class AutoViewPager extends LoopViewPager {

    private int mState;
    private final Runnable mSettler = new Runnable() {

        @Override
        public void run() {
            if (mState == SCROLL_STATE_IDLE) {
                if (getAdapter() != null && getAdapter().getCount() != 0) {
                    setCurrentItem((getCurrentItem() + 1) % getAdapter().getCount(), true);
                }
            }
        }
    };
    private int mInterval = 5000;
    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            onPageSelect();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
    };

    public AutoViewPager(Context context) {
        super(context);
        init();
    }

    public AutoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setInterval(int interval) {
        this.mInterval = interval;
    }

    private void init() {
        addOnPageChangeListener(mOnPageChangeListener);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onPageSelect();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mSettler);
    }

    public void onPageSelect() {
        removeCallbacks(mSettler);
        postDelayed(mSettler, mInterval);
    }
}

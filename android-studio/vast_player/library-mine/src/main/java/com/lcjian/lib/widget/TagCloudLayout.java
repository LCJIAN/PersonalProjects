package com.lcjian.lib.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Arrays;

public class TagCloudLayout extends ViewGroup {

    private int radius;

    private double mAngleX;
    private double mAngleY;
    private double mAngleZ;

    private double sin_mAngleX;
    private double cos_mAngleX;
    private double sin_mAngleY;
    private double cos_mAngleY;
    private double sin_mAngleZ;
    private double cos_mAngleZ;

    private TagView[] mcList;

    private int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    private OnScrollListener mOnScrollListener;

    private int mScrollState;
    private double preX;
    private double preY;
    private double preZ;
    private double downX;
    private double downY;
    private boolean allowRotating;

    public TagCloudLayout(Context context) {
        super(context);
    }

    public TagCloudLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagCloudLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OnScrollListener getOnScrollListener() {
        return mOnScrollListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    @SuppressLint("NewApi")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int witdh = Math.min(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
        int height = witdh;
        radius = (witdh - 40) / 2;
        if (mcList == null) {
            mcList = new TagView[getChildCount()];
            double phi = 0;
            double theta = 0;

            for (int i = 0; i < getChildCount(); i++) {
                mcList[i] = (TagView) getChildAt(i);
                phi = Math.acos((double) -1 + (double) (2 * (i + 1) - 1) / (double) getChildCount());
                theta = Math.sqrt(getChildCount() * Math.PI) * phi;
                // 坐标变换
                mcList[i].cx = radius * Math.cos(theta) * Math.sin(phi);
                mcList[i].cy = radius * Math.sin(theta) * Math.sin(phi);
                mcList[i].cz = radius * Math.cos(phi);

                // add perspective
                int diameter = 2 * radius;
                double per = diameter / (diameter + mcList[i].cz);

                mcList[i].scale = per;
                mcList[i].alpha = per;
                mcList[i].alpha = (mcList[i].alpha - 0.6) * (10d / 6d);
                ((TextView) mcList[i].mTarget).setTextSize((float) Math.ceil(12 * mcList[i].scale / 2) + 8);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    ((TextView) mcList[i].mTarget).setAlpha((float) mcList[i].alpha);
                }
            }
        }
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(witdh, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout((int) mcList[i].cx + getMeasuredWidth() / 2 - child.getMeasuredWidth() / 2,
                    (int) mcList[i].cy + getMeasuredHeight() / 2 - child.getMeasuredHeight() / 2,
                    (int) mcList[i].cx + getMeasuredWidth() / 2 + child.getMeasuredWidth() / 2,
                    (int) mcList[i].cy + getMeasuredHeight() / 2 + child.getMeasuredHeight() / 2);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = -(ev.getX() - radius);
                preY = ev.getY() - radius;
                downX = ev.getX();
                downY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.sqrt((ev.getX() - downX) * (ev.getX() - downX) + (ev.getY() - downY) * (ev.getY() - downY))
                        > mTouchSlop) {
                    return true;
                }
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = -(event.getX() - radius);
                preY = event.getY() - radius;
                allowRotating = false;
//            preZ = calculateZ(preX, preY);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                allowRotating = true;
                post((new FlingRunnable(mAngleX, mAngleY, mAngleZ)));
                break;
            case MotionEvent.ACTION_MOVE:
                double x = -(event.getX() - radius);
                double y = event.getY() - radius;
//            double z = calculateZ(x, y);

                double dx = x - preX;
                double dy = y - preY;
//            double dz = z - preZ;

                mAngleX = dy / radius * 2;
                mAngleY = dx / radius * 2;
                mAngleZ = 0;

                preX = x;
                preY = y;
//            preZ = z;

                rotate(mAngleX, mAngleY, mAngleZ);
                if (mOnScrollListener != null) {
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        mScrollState = OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
                        mOnScrollListener.onScrollStateChanged(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    @SuppressLint("NewApi")
    private void rotate(double angleX, double angleY, double angleZ) {
        sineCosine(angleX, angleY, angleZ);
        for (int i = 0; i < mcList.length; i++) {
            TagView child = (TagView) getChildAt(i);
            double rx1 = mcList[i].cx;
            double ry1 = mcList[i].cy * cos_mAngleX + mcList[i].cz * (-sin_mAngleX);
            double rz1 = mcList[i].cy * sin_mAngleX + mcList[i].cz * cos_mAngleX;

            double rx2 = rx1 * cos_mAngleY + rz1 * sin_mAngleY;
            double ry2 = ry1;
            double rz2 = rx1 * (-sin_mAngleY) + rz1 * cos_mAngleY;

            double rx3 = rx2 * cos_mAngleZ + ry2 * (-sin_mAngleZ);
            double ry3 = rx2 * sin_mAngleZ + ry2 * cos_mAngleZ;
            double rz3 = rz2;

            mcList[i].cx = rx3;
            mcList[i].cy = ry3;
            mcList[i].cz = rz3;

            // add perspective
            int diameter = 2 * radius;
            double per = diameter / (diameter + rz3);

            mcList[i].scale = per;
            mcList[i].alpha = per;
            mcList[i].alpha = (mcList[i].alpha - 0.6) * (10d / 6d);

            ((TextView) child.mTarget).setTextSize((float) Math.ceil(12 * mcList[i].scale / 2) + 8);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                ((TextView) child.mTarget).setAlpha((float) mcList[i].alpha);
            }
        }
        requestLayout();
        invalidate();
        depthSort();
    }

    private void sineCosine(double angleX, double angleY, double angleZ) {
        sin_mAngleX = Math.sin(angleX);
        cos_mAngleX = Math.cos(angleX);
        sin_mAngleY = Math.sin(angleY);
        cos_mAngleY = Math.cos(angleY);
        sin_mAngleZ = Math.sin(angleZ);
        cos_mAngleZ = Math.cos(angleZ);
    }

    public void depthSort() {
        Arrays.sort(mcList);
        for (TagView child : mcList) {
            bringChildToFront(child);
        }
    }

    /**
     * 计算z的坐标
     *
     * @param x coordinate x
     * @param y coordinate y
     * @return the z coordinate
     */
    public double calculateZ(double x, double y) {
        return Math.sqrt(radius * radius - (x * x + y * y));
    }

    @Override
    public void addView(View child) {
        TagView tagView = new TagView(getContext(), child);
        tagView.setLayoutParams(new LayoutParams(child.getLayoutParams()));
        super.addView(tagView);
    }

    public interface OnScrollListener {

        public static int SCROLL_STATE_IDLE = 0;

        public static int SCROLL_STATE_TOUCH_SCROLL = 1;

        public static int SCROLL_STATE_FLING = 2;

        public void onScrollStateChanged(int scrollState);
    }

    public static class TagView extends FrameLayout implements Comparable<TagView> {

        public double cx;

        public double cy;

        public double cz;

        public double scale;

        public double alpha;

        public View mTarget;

        public TagView(Context context, View target) {
            super(context);
            this.mTarget = target;
            this.addView(mTarget);
        }

        @Override
        public int compareTo(TagView another) {
            if (cz > another.cz) {
                return -1;
            } else if (cz < another.cz) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class FlingRunnable implements Runnable {

        private double angleX;

        private double angleY;

        private double angleZ;

        public FlingRunnable(double angleX, double angleY, double angleZ) {
            this.angleX = angleX;
            this.angleY = angleY;
            this.angleZ = angleZ;
        }

        public void run() {
            if (!allowRotating
                    || Math.abs(angleX) <= 0.01 && Math.abs(angleY) <= 0.01 && Math.abs(angleZ) <= 0.01) {
                if (mOnScrollListener != null) {
                    if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                        mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
                        mOnScrollListener.onScrollStateChanged(OnScrollListener.SCROLL_STATE_IDLE);
                    }
                }
                return;
            } else {
                angleX *= 0.98d;
                angleY *= 0.98d;
                angleZ *= 0.98d;
                if (mOnScrollListener != null) {
                    if (mScrollState != OnScrollListener.SCROLL_STATE_FLING) {
                        mScrollState = OnScrollListener.SCROLL_STATE_FLING;
                        mOnScrollListener.onScrollStateChanged(OnScrollListener.SCROLL_STATE_FLING);
                    }
                }
                rotate(angleX, angleY, angleZ);
                TagCloudLayout.this.post(this);
            }
        }
    }
}

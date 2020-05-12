package com.lcjian.lib.widget;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.lcjian.lib.R;

public class CircleProgressBar extends View {

    private float mPercent;
    private float mStrokeWidth;
    private int mBgColor = 0xffe1e1e1;
    private float mStartAngle = 90;
    private float mStartAngleTemp;
    private int mFgColor = 0xffffe400;

    private Context mContext;
    private RectF mOval;
    private Paint mPaint;

    private ObjectAnimator animator;
    private boolean mIndeterminate;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mContext = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, defStyleAttr, defStyleRes);
        mBgColor = a.getColor(R.styleable.CircleProgressBar_bgColor, 0xffe1e1e1);
        mFgColor = a.getColor(R.styleable.CircleProgressBar_fgColor, 0xffff4800);
        mPercent = a.getFloat(R.styleable.CircleProgressBar_percent, 20);
        mStartAngle = a.getFloat(R.styleable.CircleProgressBar_startAngle, 0) + 270;
        mStrokeWidth = a.getDimensionPixelSize(R.styleable.CircleProgressBar_strokeWidth, dp2px(21));
        a.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mOval = new RectF();
    }

    private int dp2px(float dp) {
        return (int) (mContext.getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int xp = getPaddingLeft() + getPaddingRight();
        int yp = getPaddingBottom() + getPaddingTop();
        mOval.set(getPaddingLeft() + mStrokeWidth, getPaddingTop() + mStrokeWidth,
                getPaddingLeft() + (getWidth() - xp) - mStrokeWidth,
                getPaddingTop() + (getHeight() - yp) - mStrokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(mBgColor);
        canvas.drawArc(mOval, 0, 360, false, mPaint);

        mPaint.setColor(mFgColor);
        canvas.drawArc(mOval, mStartAngle, (mIndeterminate ? 20 : mPercent) * 3.6f, false, mPaint);
    }

    public void setIndeterminate(boolean indeterminate) {
        this.mIndeterminate = indeterminate;
        if (indeterminate) {
            animateIndeterminate(2000, new LinearInterpolator());
        } else {
            stopAnimateIndeterminate();
        }
    }

    public void setStrokeWidthDp(float dp) {
        setStrokeWidth(dp2px(dp));
    }

    public float getPercent() {
        return mPercent;
    }

    public void setPercent(float mPercent) {
        this.mPercent = mPercent;
        this.mIndeterminate = false;
        stopAnimateIndeterminate();
        invalidate();
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(float mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
        requestLayout();
    }

    public int getFgColor() {
        return mFgColor;
    }

    public void setFgColor(int mFgColor) {
        this.mFgColor = mFgColor;
        invalidate();
    }

    public float getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(float mStartAngle) {
        this.mStartAngle = mStartAngle;
        invalidate();
    }

    private void animateIndeterminate(int durationOneCircle,
                                      TimeInterpolator interpolator) {
        if (animator == null) {
            mStartAngleTemp = getStartAngle();
            animator = ObjectAnimator.ofFloat(this, "startAngle", mStartAngleTemp, mStartAngleTemp + 360);
            if (interpolator != null) animator.setInterpolator(interpolator);
            animator.setDuration(durationOneCircle);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.start();
        }
    }

    private void stopAnimateIndeterminate() {
        if (animator != null) {
            animator.cancel();
            animator = null;
            mStartAngle = mStartAngleTemp;
            invalidate();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIndeterminate) {
            animateIndeterminate(2000, new LinearInterpolator());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mIndeterminate) {
            stopAnimateIndeterminate();
        }
        super.onDetachedFromWindow();
    }
}

package com.lcjian.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.lcjian.lib.R;

public class DashLine extends View {

    public static final int HORIZONTAL = 0;

    public static final int VERTICAL = 1;

    private int mOrientation;

    private int mDashWidth;

    private int mDashGap;

    private DashPathEffect mDashPathEffect;

    private int mLineColor;

    private Paint mPaint;

    private Path mPath;

    public DashLine(Context context) {
        this(context, null);
    }

    public DashLine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint();
        mPath = new Path();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DashLine, defStyleAttr, 0);

        mOrientation = a.getInt(R.styleable.DashLine_dashOrientation, 0);

        mDashGap = a.getDimensionPixelSize(R.styleable.DashLine_dashGap, 5);

        mDashWidth = a.getDimensionPixelSize(R.styleable.DashLine_dashWidth, 5);

        mLineColor = a.getColor(R.styleable.DashLine_lineColor, Color.GRAY);

        mDashPathEffect = new DashPathEffect(new float[]{mDashWidth, mDashGap}, 0);

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mLineColor);
        if (mOrientation == HORIZONTAL) {
            mPaint.setStrokeWidth(getHeight() - getPaddingBottom() - getPaddingTop());
        } else {
            mPaint.setStrokeWidth(getWidth() - getPaddingLeft() - getPaddingRight());
        }
        mPaint.setPathEffect(mDashPathEffect);

        mPath.reset();
        if (mOrientation == HORIZONTAL) {
            mPath.moveTo(getPaddingLeft(), getPaddingTop());
            mPath.lineTo(getWidth() - getPaddingRight(), getPaddingTop());
        } else {
            mPath.moveTo(getPaddingLeft(), getPaddingTop());
            mPath.lineTo(getPaddingLeft(), getHeight() - getPaddingBottom());
        }
        canvas.drawPath(mPath, mPaint);
    }
}

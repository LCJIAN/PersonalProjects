package com.org.firefighting.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class AutoPollRecyclerView extends RecyclerView {

    private Runnable mRunnable1;
    private Runnable mRunnable2;

    private int mIndex = 0;

    private boolean mRunning; //标示是否正在自动轮询
    private boolean mInter;

    public AutoPollRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mRunnable1 = new Runnable() {
            @Override
            public void run() {
                if (mRunning) {
                    if (!mInter) {
                        scrollBy(2, 2);
                    }
                    postOnAnimation(this);
                }
            }
        };
        mRunnable2 = new Runnable() {
            @Override
            public void run() {
                if (mRunning) {
                    if (!mInter) {
                        smoothScrollToPosition(++mIndex);
                    }
                    postDelayed(this, 3000);
                }
            }
        };
    }

    public void start(boolean continuously) {
        if (!mRunning) {
            post(continuously ? mRunnable1 : mRunnable2);
            mRunning = true;
        }
    }

    public void stop() {
        if (mRunning) {
            removeCallbacks(mRunnable1);
            removeCallbacks(mRunnable2);
            mRunning = false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mInter = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                mInter = false;
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // 实现渐变效果
    private Paint mPaint;
    private int mLayerId;
    private LinearGradient mLinearGradient;
    private int mPreWidth = 0; // Recyclerview宽度动态变化时，监听每一次的宽度

    public void doTopGradualEffect(final int itemViewWidth) {
        mPaint = new Paint();
        // dst_in 模式，实现底层透明度随上层透明度进行同步显示（即上层为透明时，下层就透明，并不是上层覆盖下层)
        final Xfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        mPaint.setXfermode(xfermode);
        addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDrawOver(@NotNull Canvas canvas, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {
                super.onDrawOver(canvas, parent, state);
                // 当linearGradient为空即第一次绘制 或 Recyclerview宽度发生改变时，重新计算透明位置
                if (mLinearGradient == null || mPreWidth != parent.getWidth()) {
                    // 透明位置从最后一个 itemView 的一半处到 Recyclerview 的最右边
                    mLinearGradient = new LinearGradient(parent.getWidth() - (itemViewWidth / 2f), 0.0f,
                            parent.getWidth(), 0.0f, new int[]{Color.BLACK, 0}, null, Shader.TileMode.CLAMP);
                    mPreWidth = parent.getWidth();
                }
                mPaint.setXfermode(xfermode);
                mPaint.setShader(mLinearGradient);
                canvas.drawRect(0.0f, 0.0f, parent.getRight(), parent.getBottom(), mPaint);
                mPaint.setXfermode(null);
                canvas.restoreToCount(mLayerId);
            }

            @Override
            public void onDraw(@NotNull Canvas c, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
                // 此处 Paint的参数这里传的null， 在传入 mPaint 时会出现第一次打开黑屏闪现的问题
                // 注意 saveLayer 不能省也不能移动到onDrawOver方法里
                mLayerId = c.saveLayer(0.0f, 0.0f, (float) parent.getWidth(), (float) parent.getHeight(), null, Canvas.ALL_SAVE_FLAG);
            }

            @Override
            public void getItemOffsets(@NotNull Rect outRect, @NotNull View view, @NotNull RecyclerView parent, @NotNull RecyclerView.State state) {
                // 该方法作用自行百度
                super.getItemOffsets(outRect, view, parent, state);
            }
        });
    }

    public static class ScrollSpeedLinearLayoutManger extends LinearLayoutManager {

        private float MILLISECONDS_PER_INCH = 0.03f;

        private Context context;

        public ScrollSpeedLinearLayoutManger(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

                @Override
                protected int getVerticalSnapPreference() {
                    return SNAP_TO_START;/*SNAP_TO_END*/
                }

                @Override
                public PointF computeScrollVectorForPosition(int targetPosition) {
                    return ScrollSpeedLinearLayoutManger.this.computeScrollVectorForPosition(targetPosition);
                }

                //This returns the milliseconds it takes to
                //scroll one pixel.
                @Override
                protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    setSpeedSlow();
                    return MILLISECONDS_PER_INCH / displayMetrics.density;
                    // return 700;
                    //返回滑动一个pixel需要多少毫秒
                }
            };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }

        public void setSpeedSlow() {
            //自己在这里用density去乘，希望不同分辨率设备上滑动速度相同
            //0.3f是自己估摸的一个值，可以根据不同需求自己修改
            MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().density * 1f;
        }

        public void setSpeedFast() {
            MILLISECONDS_PER_INCH = context.getResources().getDisplayMetrics().density * 0.3f;
        }
    }
}
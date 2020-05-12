package com.lcjian.lib.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.widget.GridView;

import com.lcjian.lib.R;

public class ShelfView extends GridView {
    private Bitmap mShelfBackground;
    private int mShelfWidth;
    private int mShelfHeight;

    public ShelfView(Context context) {
        super(context);
    }

    public ShelfView(Context context, AttributeSet attrs) {
        super(context, attrs);
        load(context, attrs, 0);
    }

    public ShelfView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        load(context, attrs, defStyle);
    }

    private void load(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShelvesView, defStyle, 0);

        final Resources resources = getResources();
        final int background = a.getResourceId(R.styleable.ShelvesView_shelfBackground, 0);
        final Bitmap shelfBackground = BitmapFactory.decodeResource(resources, background);
        if (shelfBackground != null) {
            mShelfWidth = shelfBackground.getWidth();
            mShelfHeight = shelfBackground.getHeight();
            mShelfBackground = shelfBackground;
        }
        a.recycle();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final int count = getChildCount();
        final int top = count > 0 ? getChildAt(0).getTop() : 0;
        final int shelfWidth = mShelfWidth;
        final int shelfHeight = mShelfHeight;
        final int width = getWidth();
        final int height = getHeight();
        final Bitmap background = mShelfBackground;

        for (int x = 0; x < width; x += shelfWidth) {
            for (int y = top; y < height; y += shelfHeight) {
                canvas.drawBitmap(background, x, y, null);
            }
        }

        super.dispatchDraw(canvas);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);

        final Drawable current = getSelector().getCurrent();
        if (current instanceof TransitionDrawable) {
            if (pressed) {
                ((TransitionDrawable) current).startTransition(
                        ViewConfiguration.getLongPressTimeout());
            } else {
                ((TransitionDrawable) current).resetTransition();
            }
        }
    }
}

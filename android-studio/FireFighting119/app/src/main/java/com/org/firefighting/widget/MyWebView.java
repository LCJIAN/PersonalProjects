package com.org.firefighting.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import timber.log.Timber;

public class MyWebView extends WebView {

    public float oldY;
    private int t;

    public MyWebView(Context context) {
        super(context);
    }

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                requestDisallowInterceptTouchEvent(true);
                oldY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float y = ev.getY();
                float deltaY = y - oldY;
                if (deltaY > 0 && t == 0) {
                    Timber.d("deltaY > 0 requestDisallowInterceptTouchEvent(false)");
                    requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                if (deltaY < 0 && (getContentHeight() * getScale() - (getHeight() + t) < 4)) {
                    Timber.d("deltaY < 0 requestDisallowInterceptTouchEvent(false)");
                    requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                requestDisallowInterceptTouchEvent(true);
                break;
            default:
                break;
        }

        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldL, int oldT) {
        this.t = t;
        super.onScrollChanged(l, t, oldL, oldT);
    }

}

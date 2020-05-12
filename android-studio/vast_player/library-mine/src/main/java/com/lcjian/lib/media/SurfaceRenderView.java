package com.lcjian.lib.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SurfaceRenderView extends SurfaceView implements IRenderView {

    private MeasureHelper mMeasureHelper;
    private SurfaceCallback mSurfaceCallback;

    public SurfaceRenderView(Context context) {
        super(context);
        initView();
    }

    public SurfaceRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SurfaceRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SurfaceRenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        mMeasureHelper = new MeasureHelper(this);
        mSurfaceCallback = new SurfaceCallback();
        getHolder().addCallback(mSurfaceCallback);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean shouldWaitForResize() {
        return true;
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            getHolder().setFixedSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    @Override
    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        if (videoSarNum > 0 && videoSarDen > 0) {
            mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            requestLayout();
        }
    }

    @Override
    public void setVideoRotation(int degree) {
        Log.e("VideoView", "SurfaceView doesn't support rotation (" + degree + ")!");
    }

    @Override
    public void setAspectRatio(int aspectRatio) {
        mMeasureHelper.setAspectRatio(aspectRatio);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }

    @Override
    public void addRenderCallback(IRenderCallback callback) {
        mSurfaceCallback.addRenderCallback(callback);
    }

    @Override
    public void removeRenderCallback(IRenderCallback callback) {
        mSurfaceCallback.removeRenderCallback(callback);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(SurfaceRenderView.class.getName());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            info.setClassName(SurfaceRenderView.class.getName());
        }
    }

    private static final class InternalSurfaceHolder implements IRenderView.ISurfaceHolder {

        private SurfaceHolder mSurfaceHolder;

        @Nullable
        @Override
        public SurfaceHolder getSurfaceHolder() {
            return mSurfaceHolder;
        }

        @Override
        public void setSurfaceHolder(@Nullable SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;
        }

        @Nullable
        @Override
        public SurfaceTexture getSurfaceTexture() {
            return null;
        }

        @Override
        public void setSurfaceTexture(@Nullable SurfaceTexture surfaceTexture) {

        }

        @Override
        public Surface getSurface() {
            return null;
        }

        @Override
        public void release() {
        }
    }

    private static final class SurfaceCallback implements SurfaceHolder.Callback {
        private ISurfaceHolder mISurfaceHolder;
        private boolean mIsFormatChanged;
        private int mFormat;
        private int mWidth;
        private int mHeight;

        private Map<IRenderCallback, Object> mRenderCallbackMap = new ConcurrentHashMap<>();

        public void addRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.put(callback, callback);

            if (mISurfaceHolder != null) {
                callback.onSurfaceCreated(mISurfaceHolder, mWidth, mHeight);
            }
            if (mIsFormatChanged) {
                if (mISurfaceHolder != null) {
                    callback.onSurfaceChanged(mISurfaceHolder, mFormat, mWidth, mHeight);
                }
            }
        }

        public void removeRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.remove(callback);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mIsFormatChanged = false;
            mFormat = 0;
            mWidth = 0;
            mHeight = 0;

            mISurfaceHolder = new InternalSurfaceHolder();
            mISurfaceHolder.setSurfaceHolder(holder);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceCreated(mISurfaceHolder, 0, 0);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsFormatChanged = false;
            mFormat = 0;
            mWidth = 0;
            mHeight = 0;

            mISurfaceHolder.setSurfaceHolder(holder);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceDestroyed(mISurfaceHolder);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mIsFormatChanged = true;
            mFormat = format;
            mWidth = width;
            mHeight = height;

            mISurfaceHolder.setSurfaceHolder(holder);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceChanged(mISurfaceHolder, format, width, height);
            }
        }
    }
}
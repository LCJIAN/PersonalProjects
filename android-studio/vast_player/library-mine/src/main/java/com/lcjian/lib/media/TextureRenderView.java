package com.lcjian.lib.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TextureRenderView extends TextureView implements IRenderView {

    private MeasureHelper mMeasureHelper;
    private SurfaceCallback mSurfaceCallback;

    public TextureRenderView(Context context) {
        super(context);
        initView();
    }

    public TextureRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        mMeasureHelper = new MeasureHelper(this);
        mSurfaceCallback = new SurfaceCallback();
        setSurfaceTextureListener(mSurfaceCallback);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean shouldWaitForResize() {
        return false;
    }

    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
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
        mMeasureHelper.setVideoRotation(degree);
        setRotation(degree);
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
        event.setClassName(TextureRenderView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TextureRenderView.class.getName());
    }

    private static final class InternalSurfaceHolder implements IRenderView.ISurfaceHolder {

        private SurfaceTexture mSurfaceTexture;

        private Surface mSurface;

        @Nullable
        @Override
        public SurfaceHolder getSurfaceHolder() {
            return null;
        }

        @Override
        public void setSurfaceHolder(@Nullable SurfaceHolder surfaceHolder) {

        }

        @Nullable
        @Override
        public SurfaceTexture getSurfaceTexture() {
            return mSurfaceTexture;
        }

        @Override
        public void setSurfaceTexture(@Nullable SurfaceTexture surfaceTexture) {
            if (mSurfaceTexture != surfaceTexture) {
                if (mSurface != null) {
                    mSurface.release();
                    mSurface = null;
                }
                mSurfaceTexture = surfaceTexture;
                if (mSurfaceTexture != null) {
                    mSurface = new Surface(mSurfaceTexture);
                }
            }
        }

        @Override
        public Surface getSurface() {
            return mSurface;
        }

        @Override
        public void release() {
            if (mSurface != null) {
                mSurface.release();
                mSurface = null;
            }
        }
    }

    private static final class SurfaceCallback implements TextureView.SurfaceTextureListener {
        private ISurfaceHolder mISurfaceHolder;
        private boolean mIsFormatChanged;
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
                    callback.onSurfaceChanged(mISurfaceHolder, 0, mWidth, mHeight);
                }
            }
        }

        public void removeRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.remove(callback);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mIsFormatChanged = false;
            mWidth = 0;
            mHeight = 0;

            mISurfaceHolder = new InternalSurfaceHolder();
            mISurfaceHolder.setSurfaceTexture(surface);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceCreated(mISurfaceHolder, 0, 0);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mIsFormatChanged = true;
            mWidth = width;
            mHeight = height;

            mISurfaceHolder.setSurfaceTexture(surface);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceChanged(mISurfaceHolder, 0, width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mIsFormatChanged = false;
            mWidth = 0;
            mHeight = 0;

            mISurfaceHolder.setSurfaceTexture(surface);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceDestroyed(mISurfaceHolder);
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }
}
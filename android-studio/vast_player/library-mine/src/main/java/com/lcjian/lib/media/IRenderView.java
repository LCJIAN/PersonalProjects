package com.lcjian.lib.media;

import android.graphics.SurfaceTexture;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

public interface IRenderView {

    int AR_ASPECT_FIT_PARENT = 0; // without clip
    int AR_ASPECT_FILL_PARENT = 1; // may clip
    int AR_ASPECT_WRAP_CONTENT = 2;
    int AR_MATCH_PARENT = 3;
    int AR_16_9_FIT_PARENT = 4;
    int AR_4_3_FIT_PARENT = 5;

    View getView();

    boolean shouldWaitForResize();

    void setVideoSize(int videoWidth, int videoHeight);

    void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen);

    void setVideoRotation(int degree);

    void setAspectRatio(int aspectRatio);

    void addRenderCallback(IRenderCallback callback);

    void removeRenderCallback(IRenderCallback callback);

    interface ISurfaceHolder {

        @Nullable
        SurfaceHolder getSurfaceHolder();

        void setSurfaceHolder(@Nullable SurfaceHolder surfaceHolder);

        @Nullable
        SurfaceTexture getSurfaceTexture();

        void setSurfaceTexture(@Nullable SurfaceTexture surfaceTexture);

        Surface getSurface();

        void release();
    }

    interface IRenderCallback {
        /**
         * @param width  could be 0
         * @param height could be 0
         */
        void onSurfaceCreated(@NonNull ISurfaceHolder holder, int width, int height);

        /**
         * @param format could be 0
         */
        void onSurfaceChanged(@NonNull ISurfaceHolder holder, int format, int width, int height);

        void onSurfaceDestroyed(@NonNull ISurfaceHolder holder);
    }
}
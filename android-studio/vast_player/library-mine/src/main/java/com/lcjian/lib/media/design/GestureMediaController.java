package com.lcjian.lib.media.design;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import androidx.core.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;

import com.lcjian.lib.media.ScreenBrightnessUtil;

public class GestureMediaController {

    private static final int ADJUST_NONE = 0;

    private static final int ADJUST_BRIGHTNESS = 1;

    private static final int ADJUST_VOLUME = 2;

    private static final int ADJUST_PROGRESS = 3;

    private int mAdjustMode = ADJUST_NONE;

    private int mRangeX;

    private int mRangeY;

    private GestureTransportController mTransportController;

    private GestureDetectorCompat mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            mTransportController.onSingleTapUp();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mTransportController.onDoubleTap();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1 == null || e2 == null) { // e1 and e2 may be null on some roms
                return true;
            }
            float deltaX = e1.getX() - e2.getX();
            float deltaY = e1.getY() - e2.getY();
            if (mAdjustMode == ADJUST_NONE) {
                float absDeltaX = Math.abs(deltaX);
                float absDeltaY = Math.abs(deltaY);
                if (absDeltaX < absDeltaY) {
                    if (e1.getX() < mRangeX / 2) {
                        mAdjustMode = ADJUST_BRIGHTNESS;
                    } else {
                        mAdjustMode = ADJUST_VOLUME;
                    }
                } else {
                    mAdjustMode = ADJUST_PROGRESS;
                }
                mTransportController.onStartAdjust();
            } else {
                if (mAdjustMode == ADJUST_BRIGHTNESS) {
                    mTransportController.adjustBrightness(deltaY * 100 / mRangeY);
                } else if (mAdjustMode == ADJUST_VOLUME) {
                    mTransportController.adjustVolume(deltaY * 100 / mRangeY);
                } else if (mAdjustMode == ADJUST_PROGRESS) {
                    mTransportController.adjustProgress(-deltaX * 100 / mRangeX);
                }
            }
            return true;
        }
    };

    public GestureMediaController(View view, GestureTransportController transportController) {
        mTransportController = transportController;
        init(view);
    }

    private void init(final View view) {
        if (mTransportController == null) {
            throw new NullPointerException("GestureMediaController must not be null");
        }
        mRangeX = view.getWidth();
        mRangeY = view.getHeight();
        view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                mRangeX = view.getWidth();
                mRangeY = view.getHeight();
            }
        });
        mGestureDetector = new GestureDetectorCompat(view.getContext(), mSimpleOnGestureListener);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                if (mAdjustMode != ADJUST_NONE) {
                    mTransportController.onStopAdjust();
                }
                mAdjustMode = ADJUST_NONE;
                break;
            default:
                break;
        }
        mGestureDetector.onTouchEvent(ev);
        return true;
    }

    public interface GestureTransportController {

        void adjustBrightness(float percentage);

        void adjustVolume(float percentage);

        void adjustProgress(float percentage);

        void onStartAdjust();

        void onStopAdjust();

        void onSingleTapUp();

        void onDoubleTap();
    }

    public static class SimpleGestureTransportController implements GestureTransportController {

        private AudioManager mAudioManager;

        private float mStartBrightness;

        private int mStartVolume;

        private int mRequestProgress;

        private View mView;

        private TransportMediator mController;

        private MediaController mMediaController;

        private UIPerformer mUIPerformer;

        public SimpleGestureTransportController(View view, TransportMediator controller, MediaController mediaController) {
            this.mView = view;
            this.mController = controller;
            this.mMediaController = mediaController;
        }

        public void setUIPerformer(UIPerformer performer) {
            this.mUIPerformer = performer;
        }

        @Override
        public void onStartAdjust() {
            mStartBrightness = ScreenBrightnessUtil.getBrightness((Activity) mView.getContext());
            if (mStartBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                mStartBrightness = ScreenBrightnessUtil.getSystemBrightness(mView.getContext().getContentResolver()) / 255f;
            }
            if (mAudioManager == null) {
                mAudioManager = (AudioManager) mView.getContext().getSystemService(Context.AUDIO_SERVICE);
            }
            mStartVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            mRequestProgress = 0;
            if (mUIPerformer != null) {
                mUIPerformer.startShow();
            }
        }

        @Override
        public void onStopAdjust() {
            if (mUIPerformer != null) {
                mUIPerformer.stopShow();
                if (mRequestProgress != 0) {
                    mUIPerformer.stopAdjustProgress(mRequestProgress);
                }
            }
        }

        @Override
        public void onSingleTapUp() {
            if (mMediaController != null) {
                if (mMediaController.isShowing()) {
                    mMediaController.hide();
                } else {
                    mMediaController.show();
                }
            }
        }

        @Override
        public void onDoubleTap() {
            if (mController.isPlaying()) {
                mController.pausePlaying();
            } else {
                mController.startPlaying();
            }
        }

        @Override
        public void adjustVolume(float percentage) {
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int requestVolume = (int) (mStartVolume + maxVolume * percentage / 100);
            if (requestVolume < 0) {
                requestVolume = 0;
            }
            if (requestVolume > maxVolume) {
                requestVolume = maxVolume;
            }
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, requestVolume, 0);

            if (mUIPerformer != null) {
                mUIPerformer.showAdjustVolume(requestVolume, maxVolume);
            }
        }

        @Override
        public void adjustProgress(float percentage) {
            int requestProgress = (int) (percentage * 600 / 100);
            long destination = mController.getCurrentPosition() + requestProgress * 1000;
            if (destination > mController.getDuration() || destination < 0) {
                return;
            }
            mRequestProgress = requestProgress;
            if (mUIPerformer != null) {
                mUIPerformer.showAdjustProgress(requestProgress);
            }
        }

        @Override
        public void adjustBrightness(float percentage) {
            float requestBrightness = mStartBrightness + percentage / 100f;
            if (requestBrightness < 0.01f) {
                requestBrightness = 0.01f;
            }
            if (requestBrightness > 1.0f) {
                requestBrightness = 1.0f;
            }
            ScreenBrightnessUtil.setBrightness(((Activity) mView.getContext()), requestBrightness);
            if (mUIPerformer != null) {
                mUIPerformer.showAdjustBrightness(requestBrightness);
            }
        }

        public interface UIPerformer {

            void stopAdjustProgress(int requestProgress);

            void showAdjustVolume(int requestVolume, int maxVolume);

            void showAdjustBrightness(float requestBrightness);

            void showAdjustProgress(int requestProgress);

            void startShow();

            void stopShow();
        }
    }
}

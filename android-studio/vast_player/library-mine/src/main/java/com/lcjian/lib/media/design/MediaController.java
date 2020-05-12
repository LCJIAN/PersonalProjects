/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lcjian.lib.media.design;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;

/**
 * Helper for implementing media controls in an application.
 * Use instead of the very useful android.widget.MediaController.
 * This version is embedded inside of an application's layout.
 */
public class MediaController extends FrameLayout {

    private static final int sDefaultTimeout = 3000;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private TransportMediator mController;
    private Context mContext;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private boolean mDragging;
    private boolean mUseFastForward;
    private boolean mListenersSet;
    private boolean mShowNext, mShowPrev;
    private OnClickListener mNextListener, mPrevListener;
    private ImageButton mPauseButton;
    private ImageButton mFfwdButton;
    private ImageButton mRewButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;

    private OnVisibilityChangeListener mOnVisibilityChangeListener;
    private TransportMediator.TransportStateListener mTransportStateListener;
    private TransportMediator.TransportStateListener mStateListener = new TransportMediator.TransportStateListener() {
        @Override
        public void onPlayingChanged(TransportMediator controller) {
            if (mTransportStateListener != null) {
                mTransportStateListener.onPlayingChanged(controller);
            }
            updatePausePlay();
        }

        @Override
        public void onTransportControlsChanged(TransportMediator controller) {
            if (mTransportStateListener != null) {
                mTransportStateListener.onTransportControlsChanged(controller);
            }
            updateButtons();
        }
    };
    private Runnable mProgressUpdater = new Runnable() {
        @Override
        public void run() {
            long pos = updateProgress();
            if (!mDragging && isShowing() && mController.isPlaying()) {
                getHandler().postDelayed(this, 1000 - (pos % 1000));
            }
        }
    };
    private Runnable mNavHider = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
        }
    };
    // There are two scenarios that can trigger the SeekBar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the position of the
    // SeekBar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            if (!fromUser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mController.getDuration();
            long newPosition = (duration * progress) / 1000L;
            mController.seekTo((int) newPosition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newPosition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            updateProgress();
            updatePausePlay();
        }
    };
    private OnClickListener mRewListener = new OnClickListener() {
        public void onClick(View v) {
            long pos = mController.getCurrentPosition();
            pos -= 5000; // milliseconds
            mController.seekTo(pos);
            updateProgress();
        }
    };
    private OnClickListener mFfwdListener = new OnClickListener() {
        public void onClick(View v) {
            long pos = mController.getCurrentPosition();
            pos += 15000; // milliseconds
            mController.seekTo(pos);
            updateProgress();
        }
    };

    public MediaController(Context context) {
        this(context, null);
    }

    public MediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mUseFastForward = true;
        init();
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(getResources().getIdentifier(layoutResource(), "layout", getContext().getPackageName()), this, true);
        initControllerView();
    }

    protected String layoutResource() {
        return "media_controller";
    }

    public void setTransportStateListener(TransportMediator.TransportStateListener transportStateListener) {
        this.mTransportStateListener = transportStateListener;
    }

    public void setMediaPlayer(TransportMediator controller) {
        if (getWindowToken() != null) {
            if (mController != null) {
                mController.unregisterStateListener(mStateListener);
            }
            if (controller != null) {
                controller.registerStateListener(mStateListener);
            }
        }
        mController = controller;
        updatePausePlay();
    }

    public void setUseFastForward(boolean useFastForward) {
        mUseFastForward = useFastForward;
        mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
        mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mController != null) {
            mController.registerStateListener(mStateListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mController != null) {
            mController.unregisterStateListener(mStateListener);
        }
    }

    private void initControllerView() {
        mPauseButton = findViewById(getResources().getIdentifier("pause", "id", getContext().getPackageName()));
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mFfwdButton = findViewById(getResources().getIdentifier("ffwd", "id", getContext().getPackageName()));
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(mFfwdListener);
            mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
        }

        mRewButton = findViewById(getResources().getIdentifier("rew", "id", getContext().getPackageName()));
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
            mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
        }

        // By default these are hidden. They will be enabled when setPrevNextListeners() is called
        mNextButton = findViewById(getResources().getIdentifier("next", "id", getContext().getPackageName()));
        if (mNextButton != null && !mListenersSet) {
            mNextButton.setVisibility(View.GONE);
        }
        mPrevButton = findViewById(getResources().getIdentifier("prev", "id", getContext().getPackageName()));
        if (mPrevButton != null && !mListenersSet) {
            mPrevButton.setVisibility(View.GONE);
        }

        mProgress = findViewById(getResources().getIdentifier("media_controller_progress", "id", getContext().getPackageName()));
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = findViewById(getResources().getIdentifier("time", "id", getContext().getPackageName()));
        mCurrentTime = findViewById(getResources().getIdentifier("time_current", "id", getContext().getPackageName()));
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        installPrevNextListeners();
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void updateButtons() {
        int flags = mController.getTransportControlFlags();
        boolean enabled = isEnabled();
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled && (flags & TransportMediator.FLAG_KEY_MEDIA_PAUSE) != 0);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled && (flags & TransportMediator.FLAG_KEY_MEDIA_REWIND) != 0);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled && (flags & TransportMediator.FLAG_KEY_MEDIA_FAST_FORWARD) != 0);
        }
        if (mPrevButton != null) {
            mShowPrev = (flags & TransportMediator.FLAG_KEY_MEDIA_PREVIOUS) != 0 || mPrevListener != null;
            mPrevButton.setEnabled(enabled && mShowPrev);
        }
        if (mNextButton != null) {
            mShowNext = (flags & TransportMediator.FLAG_KEY_MEDIA_NEXT) != 0 || mNextListener != null;
            mNextButton.setEnabled(enabled && mShowNext);
        }
    }

    public void refresh() {
        updateProgress();
        updateButtons();
        updatePausePlay();
    }

    /**
     * Hide the controller.
     */
    public void hide() {
        setNavVisibility(false);
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        setNavVisibility(true);
    }

    protected void doHide() {
        setVisibility(INVISIBLE);
    }

    protected void doShow() {
        setVisibility(VISIBLE);
    }

    private void setNavVisibility(boolean visible) {
        Handler h = getHandler();
        // If we are now visible, schedule a timer for us to go invisible.
        if (visible) {
            if (h != null) {
                h.removeCallbacks(mNavHider);
                if (mController.isPlaying()) {
                    // If the menus are open or play is paused, we will not
                    // auto-hide.
                    h.postDelayed(mNavHider, sDefaultTimeout);
                }
                h.removeCallbacks(mProgressUpdater);
                h.post(mProgressUpdater);
            }
        } else {
            if (h != null) {
                h.removeCallbacks(mProgressUpdater);
            }
        }
        if (visible) {
            doShow();
        } else {
            doHide();
        }
        if (mOnVisibilityChangeListener != null) {
            mOnVisibilityChangeListener.onVisibilityChange(visible);
        }
    }

    public boolean isShowing() {
        return getVisibility() == View.VISIBLE;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private long updateProgress() {
        if (mController == null || mDragging) {
            return 0;
        }
        long position = mController.getCurrentPosition();
        long duration = mController.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mController.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime((int) duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime((int) position));

        return position;
    }

    private void updatePausePlay() {
        if (mPauseButton == null)
            return;

        if (mController.isPlaying()) {
            mPauseButton.setImageResource(getResources().getIdentifier("ic_media_pause", "drawable", getContext().getPackageName()));
        } else {
            mPauseButton.setImageResource(getResources().getIdentifier("ic_media_play", "drawable", getContext().getPackageName()));
        }
    }

    private void doPauseResume() {
        if (mController.isPlaying()) {
            mController.pausePlaying();
        } else {
            mController.startPlaying();
        }
        updatePausePlay();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateButtons();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(MediaController.class.getName());
    }

    @Override
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(MediaController.class.getName());
    }

    private void installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
            mNextButton.setEnabled(mShowNext);
        }

        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
            mPrevButton.setEnabled(mShowPrev);
        }
    }

    public void setPrevNextListeners(OnClickListener next, OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;
        mListenersSet = true;

        installPrevNextListeners();

        if (mNextButton != null) {
            mNextButton.setVisibility(View.VISIBLE);
            mShowNext = true;
        }
        if (mPrevButton != null) {
            mPrevButton.setVisibility(View.VISIBLE);
            mShowPrev = true;
        }
    }

    public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
        this.mOnVisibilityChangeListener = listener;
    }

    public interface OnVisibilityChangeListener {
        /**
         * Called when the MediaController visibility has changed.
         *
         * @param visible True if the MediaController is visible.
         */
        void onVisibilityChange(boolean visible);
    }
}

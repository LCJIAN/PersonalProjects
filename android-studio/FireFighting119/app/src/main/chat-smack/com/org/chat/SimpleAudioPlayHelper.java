package com.org.chat;

import android.media.MediaPlayer;
import android.util.LruCache;

import java.util.Map.Entry;

public class SimpleAudioPlayHelper {

    private static final LruCache<String, SimpleAudioPlayHelper> INSTANCES =
            new LruCache<String, SimpleAudioPlayHelper>(3) {
                @Override
                protected void entryRemoved(boolean evicted, String key, SimpleAudioPlayHelper oldValue, SimpleAudioPlayHelper newValue) {
                    oldValue.mMediaPlayer.setOnPreparedListener(null);
                    oldValue.mMediaPlayer.setOnCompletionListener(null);
                    oldValue.mMediaPlayer.stop();
                    oldValue.mMediaPlayer.release();
                }
            };

    private MediaPlayer mMediaPlayer;

    private Listener mListener;

    private String mPath;

    public static synchronized void evictAll() {
        INSTANCES.evictAll();
    }

    public static synchronized SimpleAudioPlayHelper getInstanceFor(String path) {
        SimpleAudioPlayHelper simpleAudioPlayHelper = INSTANCES.get(path);
        if (simpleAudioPlayHelper == null) {
            simpleAudioPlayHelper = new SimpleAudioPlayHelper(path);
            INSTANCES.put(path, simpleAudioPlayHelper);
        }
        return simpleAudioPlayHelper;
    }

    private SimpleAudioPlayHelper(String path) {
        mPath = path;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mp -> {
            mMediaPlayer.start();
            if (mListener != null) {
                mListener.onPlay();
            }
        });
        mMediaPlayer.setOnCompletionListener(mp -> {
            if (mListener != null) {
                mListener.onIdle();
            }
        });
    }

    public void startPlayVoice() {
        stopPlayVoiceOthers();
        playVoice();
    }

    private void stopPlayVoiceOthers() {
        for (Entry<String, SimpleAudioPlayHelper> entry : INSTANCES.snapshot().entrySet()) {
            if (!entry.getKey().equals(mPath)) {
                stopPlayVoice(entry.getKey());
            }
        }
    }

    private void stopPlayVoice(String path) {
        SimpleAudioPlayHelper simpleAudioPlayHelper = SimpleAudioPlayHelper.getInstanceFor(path);
        if (simpleAudioPlayHelper.mMediaPlayer.isPlaying()) {
            simpleAudioPlayHelper.mMediaPlayer.stop();
            if (simpleAudioPlayHelper.mListener != null) {
                simpleAudioPlayHelper.mListener.onIdle();
            }
        }
    }

    private void playVoice() {
        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.seekTo(0);
                if (mListener != null) {
                    mListener.onPlay();
                }
            } else {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(mPath);
                mMediaPlayer.prepareAsync();
                if (mListener != null) {
                    mListener.onPrepare();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onPrepareFailed();
            }
        }
    }

    public interface Listener {
        void onPrepare();

        void onPrepareFailed();

        void onPlay();

        void onIdle();
    }

    public static class SimpleListener implements Listener {

        @Override
        public void onPrepare() {
        }

        @Override
        public void onPrepareFailed() {
        }

        @Override
        public void onPlay() {
        }

        @Override
        public void onIdle() {
        }
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

}

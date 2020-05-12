package com.lcjian.lib.media.design;

import java.util.ArrayList;

public class TransportMediator {

    public final static int FLAG_KEY_MEDIA_PREVIOUS = 1;
    public final static int FLAG_KEY_MEDIA_REWIND = 1 << 1;
    public final static int FLAG_KEY_MEDIA_PLAY = 1 << 2;
    public final static int FLAG_KEY_MEDIA_PLAY_PAUSE = 1 << 3;
    public final static int FLAG_KEY_MEDIA_PAUSE = 1 << 4;
    public final static int FLAG_KEY_MEDIA_STOP = 1 << 5;
    public final static int FLAG_KEY_MEDIA_FAST_FORWARD = 1 << 6;
    public final static int FLAG_KEY_MEDIA_NEXT = 1 << 7;
    private final TransportPerformer mCallbacks;
    private final ArrayList<TransportStateListener> mListeners = new ArrayList<>();

    public TransportMediator(TransportPerformer callbacks) {
        mCallbacks = callbacks;
    }

    public void registerStateListener(TransportStateListener listener) {
        mListeners.add(listener);
    }

    public void unregisterStateListener(TransportStateListener listener) {
        mListeners.remove(listener);
    }

    private TransportStateListener[] getListeners() {
        if (mListeners.size() <= 0) {
            return null;
        }
        TransportStateListener listeners[] = new TransportStateListener[mListeners.size()];
        mListeners.toArray(listeners);
        return listeners;
    }

    private void reportPlayingChanged() {
        TransportStateListener[] listeners = getListeners();
        if (listeners != null) {
            for (TransportStateListener listener : listeners) {
                listener.onPlayingChanged(this);
            }
        }
    }

    private void reportTransportControlsChanged() {
        TransportStateListener[] listeners = getListeners();
        if (listeners != null) {
            for (TransportStateListener listener : listeners) {
                listener.onTransportControlsChanged(this);
            }
        }
    }

    public void refreshState() {
        reportPlayingChanged();
        reportTransportControlsChanged();
    }

    public void startPlaying() {
        mCallbacks.onStart();
        reportPlayingChanged();
    }

    public void pausePlaying() {
        mCallbacks.onPause();
        reportPlayingChanged();
    }

    public void stopPlaying() {
        mCallbacks.onStop();
        reportPlayingChanged();
    }

    public long getDuration() {
        return mCallbacks.onGetDuration();
    }

    public long getCurrentPosition() {
        return mCallbacks.onGetCurrentPosition();
    }

    public void seekTo(long pos) {
        mCallbacks.onSeekTo(pos);
    }

    public boolean isPlaying() {
        return mCallbacks.onIsPlaying();
    }

    public int getBufferPercentage() {
        return mCallbacks.onGetBufferPercentage();
    }

    /**
     * Retrieve the flags for the media transport control buttons that this transport supports.
     * Result is a combination of the following flags:
     * {@link #FLAG_KEY_MEDIA_PREVIOUS},
     * {@link #FLAG_KEY_MEDIA_REWIND},
     * {@link #FLAG_KEY_MEDIA_PLAY},
     * {@link #FLAG_KEY_MEDIA_PLAY_PAUSE},
     * {@link #FLAG_KEY_MEDIA_PAUSE},
     * {@link #FLAG_KEY_MEDIA_STOP},
     * {@link #FLAG_KEY_MEDIA_FAST_FORWARD},
     * {@link #FLAG_KEY_MEDIA_NEXT}
     */
    public int getTransportControlFlags() {
        return mCallbacks.onGetTransportControlFlags();
    }

    public interface TransportStateListener {

        void onPlayingChanged(TransportMediator controller);

        void onTransportControlsChanged(TransportMediator controller);
    }

    public interface TransportPerformer {

        void onStart();

        void onPause();

        void onStop();

        long onGetDuration();

        long onGetCurrentPosition();

        void onSeekTo(long var1);

        boolean onIsPlaying();

        int onGetBufferPercentage();

        int onGetTransportControlFlags();

    }

}

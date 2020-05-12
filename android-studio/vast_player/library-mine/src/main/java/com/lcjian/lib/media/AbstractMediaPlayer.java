package com.lcjian.lib.media;

import android.os.AsyncTask;
import android.os.Handler;

import com.lcjian.lib.media.subtitle.Caption;
import com.lcjian.lib.media.subtitle.FormatASS;
import com.lcjian.lib.media.subtitle.FormatSCC;
import com.lcjian.lib.media.subtitle.FormatSRT;
import com.lcjian.lib.media.subtitle.FormatSTL;
import com.lcjian.lib.media.subtitle.FormatTTML;
import com.lcjian.lib.media.subtitle.TimedTextObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractMediaPlayer implements IMediaPlayer {

    private OnPreparedListener mOnPreparedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnExternalTimedTextListener mOnExternalTimedTextListener;
    private TimedTextObject mTimedText;
    private Handler mTimedTextHandler;
    private Runnable mTimedTextUpdater;

    @Override
    public final void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    @Override
    public final void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    @Override
    public final void setOnBufferingUpdateListener(
            OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    @Override
    public final void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
    }

    @Override
    public final void setOnVideoSizeChangedListener(
            OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }

    @Override
    public final void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    @Override
    public final void setOnInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    @Override
    public final void setOnExternalTimedTextListener(OnExternalTimedTextListener listener) {
        mOnExternalTimedTextListener = listener;
    }

    public void resetListeners() {
        mOnPreparedListener = null;
        mOnBufferingUpdateListener = null;
        mOnCompletionListener = null;
        mOnSeekCompleteListener = null;
        mOnVideoSizeChangedListener = null;
        mOnErrorListener = null;
        mOnInfoListener = null;
        mOnExternalTimedTextListener = null;
    }

    protected final void notifyOnPrepared() {
        if (mOnPreparedListener != null)
            mOnPreparedListener.onPrepared(this);
    }

    protected final void notifyOnCompletion() {
        if (mOnCompletionListener != null)
            mOnCompletionListener.onCompletion(this);
    }

    protected final void notifyOnBufferingUpdate(int percent) {
        if (mOnBufferingUpdateListener != null)
            mOnBufferingUpdateListener.onBufferingUpdate(this, percent);
    }

    protected final void notifyOnSeekComplete() {
        if (mOnSeekCompleteListener != null)
            mOnSeekCompleteListener.onSeekComplete(this);
    }

    protected final void notifyOnVideoSizeChanged(int width, int height, int sarNum, int sarDen) {
        if (mOnVideoSizeChangedListener != null)
            mOnVideoSizeChangedListener.onVideoSizeChanged(this, width, height, sarNum, sarDen);
    }

    protected final boolean notifyOnError(int what, int extra) {
        return mOnErrorListener != null && mOnErrorListener.onError(this, what, extra);
    }

    protected final boolean notifyOnInfo(int what, int extra) {
        return mOnInfoListener != null && mOnInfoListener.onInfo(this, what, extra);
    }

    public void setDataSource(IMediaDataSource mediaDataSource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addExternalTimedTextSource(File file) {
        if (mTimedText != null) {
            mTimedTextHandler.removeCallbacks(mTimedTextUpdater);
            mTimedText = null;
            mTimedTextHandler = null;
            mTimedTextUpdater = null;
        }
        if (file != null) {
            new TimedTextProcessTask().execute(file);
        }
    }

    @Override
    public void start() throws IllegalStateException {
        startPlayExternalTimedText();
    }

    @Override
    public void seekTo(long millis) throws IllegalStateException {
        startPlayExternalTimedText();
    }

    private void startPlayExternalTimedText() {
        if (mTimedText != null && mTimedTextHandler != null && mTimedTextUpdater != null) {
            mTimedTextHandler.removeCallbacks(mTimedTextUpdater);
            initTimedTextWorker();
            mTimedTextHandler.post(mTimedTextUpdater);
        }
    }

    private void initTimedTextWorker() {
        mTimedTextHandler = new Handler();
        mTimedTextUpdater = new Runnable() {

            private Map.Entry<Integer, Caption> mCurrentEntry;

            private int mStatus;

            private boolean inCursor(long position, Map.Entry<Integer, Caption> cursor) {
                return position >= cursor.getValue().start.getMseconds()
                        && position <= cursor.getValue().end.getMseconds();
            }

            private boolean beforeCursor(long position, Map.Entry<Integer, Caption> cursor) {
                return position < cursor.getValue().start.getMseconds();
            }

            private boolean afterCursor(long position, Map.Entry<Integer, Caption> cursor) {
                return position > cursor.getValue().end.getMseconds();
            }

            @Override
            public void run() {
                if (isPlaying() && mTimedText != null && mTimedTextHandler != null && mTimedTextUpdater != null) {
                    if (mStatus == 0) {
                        long currentPos = getCurrentPosition();
                        Map.Entry<Integer, Caption> cursor = mTimedText.captions.firstEntry();
                        while (true) {
                            if (cursor == null) {
                                break;
                            }
                            if (afterCursor(currentPos, cursor)) {
                                cursor = mTimedText.captions.higherEntry(cursor.getKey());
                            } else if (inCursor(currentPos, cursor)) {
                                mCurrentEntry = cursor;

                                if (mOnExternalTimedTextListener != null) {
                                    mOnExternalTimedTextListener.onExternalTimedText(mCurrentEntry.getValue());
                                }
                                mStatus = 1;
                                mTimedTextHandler.postDelayed(mTimedTextUpdater,
                                        mCurrentEntry.getValue().end.getMseconds() - currentPos);
                                break;
                            } else if (beforeCursor(currentPos, cursor)) {
                                mCurrentEntry = cursor;

                                mStatus = 2;
                                mTimedTextHandler.postDelayed(mTimedTextUpdater,
                                        mCurrentEntry.getValue().start.getMseconds() - currentPos);
                                break;
                            }
                        }
                    } else if (mStatus == 1) {
                        if (mOnExternalTimedTextListener != null) {
                            mOnExternalTimedTextListener.onExternalTimedText(null);
                        }
                        mCurrentEntry = mTimedText.captions.higherEntry(mCurrentEntry.getKey());

                        if (mCurrentEntry != null) {
                            mStatus = 2;
                            mTimedTextHandler.postDelayed(mTimedTextUpdater,
                                    mCurrentEntry.getValue().start.getMseconds() - getCurrentPosition());
                        }
                    } else if (mStatus == 2) {
                        if (mOnExternalTimedTextListener != null) {
                            mOnExternalTimedTextListener.onExternalTimedText(mCurrentEntry.getValue());
                        }
                        mStatus = 1;
                        mTimedTextHandler.postDelayed(mTimedTextUpdater,
                                mCurrentEntry.getValue().end.getMseconds() - getCurrentPosition());
                    }
                } else {
                    mTimedTextHandler.removeCallbacks(mTimedTextUpdater);
                }
            }
        };
    }

    private class TimedTextProcessTask extends AsyncTask<File, Long, TimedTextObject> {

        @Override
        protected TimedTextObject doInBackground(File... params) {
            File file = params[0];
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(file);
                String fileName = file.getName();
                if (fileName.endsWith(".ass")) {
                    return new FormatASS().parseFile(fileName, stream);
                } else if (fileName.endsWith(".scc")) {
                    return new FormatSCC().parseFile(fileName, stream);
                } else if (fileName.endsWith(".srt")) {
                    return new FormatSRT().parseFile(fileName, stream);
                } else if (fileName.endsWith(".stl")) {
                    return new FormatSTL().parseFile(fileName, stream);
                } else if (fileName.endsWith(".ttml")) {
                    return new FormatTTML().parseFile(fileName, stream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(TimedTextObject result) {
            mTimedText = result;
            if (mTimedText != null) {
                initTimedTextWorker();
                if (isPlaying()) {
                    mTimedTextHandler.post(mTimedTextUpdater);
                }
            }
        }
    }
}

package com.lcjian.lib.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IjkWrapMediaPlayer extends AbstractMediaPlayer {
    private static MediaInfo sMediaInfo;
    private final IjkMediaPlayer mInternalMediaPlayer;
    private final AndroidMediaPlayerListenerHolder mInternalListenerAdapter;
    private final Object mInitLock = new Object();
    private tv.danmaku.ijk.media.player.misc.IMediaDataSource mMediaDataSource;
    private boolean mIsReleased;

    public IjkWrapMediaPlayer(boolean usingMediaCodec) {
        synchronized (mInitLock) {
            mInternalMediaPlayer = new IjkMediaPlayer();
            if (usingMediaCodec) {
                mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
            } else {
                mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
            }
            mInternalMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        }
        mInternalMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mInternalListenerAdapter = new AndroidMediaPlayerListenerHolder(this);
        attachInternalListeners();
    }

    public IjkMediaPlayer getInternalMediaPlayer() {
        return mInternalMediaPlayer;
    }

    @Override
    public void setDisplay(IRenderView.ISurfaceHolder sh) {
        synchronized (mInitLock) {
            if (!mIsReleased) {
                if (sh == null) {
                    mInternalMediaPlayer.setDisplay(null);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && sh.getSurfaceTexture() != null) {
                        mInternalMediaPlayer.setSurface(sh.getSurface());
                    } else if (sh.getSurfaceHolder() != null) {
                        mInternalMediaPlayer.setDisplay(sh.getSurfaceHolder());
                    } else {
                        mInternalMediaPlayer.setDisplay(null);
                    }
                }
            }
        }
    }

    @Override
    public void setDataSource(Context context, Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(context, uri);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(context, uri, headers);
    }

    @Override
    public void setDataSource(FileDescriptor fd)
            throws IOException, IllegalArgumentException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(fd);
    }

    @Override
    public void setDataSource(String path) throws IOException,
            IllegalArgumentException, SecurityException, IllegalStateException {
        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        if (!TextUtils.isEmpty(scheme) && scheme.equalsIgnoreCase("file")) {
            mInternalMediaPlayer.setDataSource(uri.getPath());
        } else {
            mInternalMediaPlayer.setDataSource(path);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void setDataSource(IMediaDataSource mediaDataSource) {
        releaseMediaDataSource();

        mMediaDataSource = new MediaDataSourceProxy(mediaDataSource);
        mInternalMediaPlayer.setDataSource(mMediaDataSource);
    }

    private void releaseMediaDataSource() {
        if (mMediaDataSource != null) {
            try {
                mMediaDataSource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaDataSource = null;
        }
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mInternalMediaPlayer.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        mInternalMediaPlayer.start();
        super.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        mInternalMediaPlayer.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        mInternalMediaPlayer.pause();
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        mInternalMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    @Override
    public int getVideoWidth() {
        return mInternalMediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mInternalMediaPlayer.getVideoHeight();
    }

    @Override
    public int getVideoSarNum() {
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        return 1;
    }

    @Override
    public boolean isPlaying() {
        try {
            return mInternalMediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void seekTo(long millis) throws IllegalStateException {
        mInternalMediaPlayer.seekTo((int) millis);
        super.seekTo(millis);
    }

    @Override
    public long getCurrentPosition() {
        return mInternalMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mInternalMediaPlayer.getDuration();
    }

    @Override
    public void release() {
        mIsReleased = true;
        mInternalMediaPlayer.release();
        releaseMediaDataSource();
        resetListeners();
        attachInternalListeners();
    }

    @Override
    public void reset() {
        mInternalMediaPlayer.reset();
        releaseMediaDataSource();
        resetListeners();
        attachInternalListeners();
    }

    @Override
    public boolean isLooping() {
        return mInternalMediaPlayer.isLooping();
    }

    @Override
    public void setLooping(boolean looping) {
        mInternalMediaPlayer.setLooping(looping);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mInternalMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public int getAudioSessionId() {
        return mInternalMediaPlayer.getAudioSessionId();
    }

    @Override
    public void setAudioSessionId(int audioSessionId) {
    }

    @Override
    public MediaInfo getMediaInfo() {
        if (sMediaInfo == null) {
            MediaInfo module = new MediaInfo();

            module.mVideoDecoder = "android";
            module.mVideoDecoderImpl = "HW";

            module.mAudioDecoder = "android";
            module.mAudioDecoderImpl = "HW";

            module.mMediaPlayerName = getClass().getSimpleName();

            sMediaInfo = module;
        }

        return sMediaInfo;
    }

    /*--------------------
     * misc
     */
    @Override
    public void setWakeMode(Context context, int mode) {
        mInternalMediaPlayer.setWakeMode(context, mode);
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        mInternalMediaPlayer.setAudioStreamType(streamtype);
    }

    /*--------------------
     * Listeners adapter
     */
    private void attachInternalListeners() {
        mInternalMediaPlayer.setOnPreparedListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnBufferingUpdateListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnCompletionListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnSeekCompleteListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnVideoSizeChangedListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnErrorListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnInfoListener(mInternalListenerAdapter);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static class MediaDataSourceProxy implements tv.danmaku.ijk.media.player.misc.IMediaDataSource {
        private final IMediaDataSource mMediaDataSource;

        public MediaDataSourceProxy(IMediaDataSource mediaDataSource) {
            mMediaDataSource = mediaDataSource;
        }

        @Override
        public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
            return mMediaDataSource.readAt(position, buffer, offset, size);
        }

        @Override
        public long getSize() throws IOException {
            return mMediaDataSource.getSize();
        }

        @Override
        public void close() throws IOException {
            mMediaDataSource.close();
        }
    }

    private class AndroidMediaPlayerListenerHolder implements
            IjkMediaPlayer.OnPreparedListener,
            IjkMediaPlayer.OnCompletionListener,
            IjkMediaPlayer.OnBufferingUpdateListener,
            IjkMediaPlayer.OnSeekCompleteListener,
            IjkMediaPlayer.OnVideoSizeChangedListener,
            IjkMediaPlayer.OnErrorListener,
            IjkMediaPlayer.OnInfoListener {
        public final WeakReference<IjkWrapMediaPlayer> mWeakMediaPlayer;

        public AndroidMediaPlayerListenerHolder(IjkWrapMediaPlayer mp) {
            mWeakMediaPlayer = new WeakReference<>(mp);
        }

        @Override
        public void onBufferingUpdate(tv.danmaku.ijk.media.player.IMediaPlayer mp, int percent) {
            IjkWrapMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnBufferingUpdate(percent);
        }

        @Override
        public void onCompletion(tv.danmaku.ijk.media.player.IMediaPlayer mp) {
            IjkWrapMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnCompletion();
        }

        @Override
        public boolean onError(tv.danmaku.ijk.media.player.IMediaPlayer mp, int what, int extra) {
            IjkWrapMediaPlayer self = mWeakMediaPlayer.get();
            return self != null && notifyOnError(what, extra);
        }

        @Override
        public boolean onInfo(tv.danmaku.ijk.media.player.IMediaPlayer mp, int what, int extra) {
            IjkWrapMediaPlayer self = mWeakMediaPlayer.get();
            return self != null && notifyOnInfo(what, extra);
        }

        @Override
        public void onPrepared(tv.danmaku.ijk.media.player.IMediaPlayer mp) {
            IjkWrapMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnPrepared();
        }

        @Override
        public void onSeekComplete(tv.danmaku.ijk.media.player.IMediaPlayer mp) {
            IjkWrapMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnSeekComplete();
        }

        @Override
        public void onVideoSizeChanged(tv.danmaku.ijk.media.player.IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            IjkWrapMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnVideoSizeChanged(width, height, sar_num, sar_den);
        }
    }
}

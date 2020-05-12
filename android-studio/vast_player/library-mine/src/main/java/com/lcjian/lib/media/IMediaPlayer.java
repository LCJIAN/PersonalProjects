package com.lcjian.lib.media;

import android.content.Context;
import android.net.Uri;

import com.lcjian.lib.media.subtitle.Caption;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

public interface IMediaPlayer {

    /*
     * Do not change these values without updating their counterparts in native
     */
    int MEDIA_INFO_UNKNOWN = 1;
    int MEDIA_INFO_STARTED_AS_NEXT = 2;
    int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    int MEDIA_INFO_BUFFERING_START = 701;
    int MEDIA_INFO_BUFFERING_END = 702;
    int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    int MEDIA_INFO_BAD_INTERLEAVING = 800;
    int MEDIA_INFO_NOT_SEEKABLE = 801;
    int MEDIA_INFO_METADATA_UPDATE = 802;
    int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
    int MEDIA_INFO_DOWNLOAD_RATE_CHANGED = 1000;

    int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;
    int MEDIA_INFO_AUDIO_RENDERING_START = 10002;

    int MEDIA_ERROR_UNKNOWN = 1;
    int MEDIA_ERROR_SERVER_DIED = 100;
    int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    int MEDIA_ERROR_IO = -1004;
    int MEDIA_ERROR_MALFORMED = -1007;
    int MEDIA_ERROR_UNSUPPORTED = -1010;
    int MEDIA_ERROR_TIMED_OUT = -110;

    void setDisplay(IRenderView.ISurfaceHolder sh);

    void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException;

    void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void prepareAsync() throws IllegalStateException;

    void start() throws IllegalStateException;

    void stop() throws IllegalStateException;

    void pause() throws IllegalStateException;

    void setWakeMode(Context context, int mode);

    void setScreenOnWhilePlaying(boolean screenOn);

    int getVideoWidth();

    int getVideoHeight();

    boolean isPlaying();

    void seekTo(long millis) throws IllegalStateException;

    long getCurrentPosition();

    long getDuration();

    void release();

    void reset();

    void setVolume(float leftVolume, float rightVolume);

    MediaInfo getMediaInfo();

    int getAudioSessionId();

    void setAudioSessionId(int audioSessionId);

    void setOnPreparedListener(OnPreparedListener listener);

    void setOnCompletionListener(OnCompletionListener listener);

    void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

    void setOnSeekCompleteListener(OnSeekCompleteListener listener);

    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

    void setOnErrorListener(OnErrorListener listener);

    void setOnInfoListener(OnInfoListener listener);

    void setOnExternalTimedTextListener(OnExternalTimedTextListener listener);

    /*
     * Optional
     */
    void setAudioStreamType(int streamType);

    int getVideoSarNum();

    int getVideoSarDen();

    boolean isLooping();

    void setLooping(boolean looping);

    /*
     * AndroidMediaPlayer: M:
     */
    void setDataSource(IMediaDataSource mediaDataSource);

    void addExternalTimedTextSource(File file);

    /*
     * Listeners
     */
    interface OnPreparedListener {
        void onPrepared(IMediaPlayer mp);
    }

    interface OnCompletionListener {
        void onCompletion(IMediaPlayer mp);
    }

    interface OnBufferingUpdateListener {
        void onBufferingUpdate(IMediaPlayer mp, int percent);
    }

    interface OnSeekCompleteListener {
        void onSeekComplete(IMediaPlayer mp);
    }

    interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den);
    }

    interface OnErrorListener {
        boolean onError(IMediaPlayer mp, int what, int extra);
    }

    /*
     * AndroidMediaPlayer: JELLY_BEAN
     */
//    ITrackInfo[] getTrackInfo();

    interface OnInfoListener {
        boolean onInfo(IMediaPlayer mp, int what, int extra);
    }

    interface OnExternalTimedTextListener {
        void onExternalTimedText(Caption caption);
    }
}
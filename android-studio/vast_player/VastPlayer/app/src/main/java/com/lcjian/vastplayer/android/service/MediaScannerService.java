package com.lcjian.vastplayer.android.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.lcjian.lib.util.Environment;
import com.lcjian.lib.util.common.FileUtils;
import com.lcjian.lib.util.common.MimeUtils;
import com.lcjian.vastplayer.App;
import com.lcjian.vastplayer.data.db.AppDatabase;
import com.lcjian.vastplayer.data.db.entity.VideoLocal;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import timber.log.Timber;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 媒体扫描
 */
public class MediaScannerService extends Service implements Runnable {

    public static final String EXTRA_DIRECTORY = "scan_directory";
    public static final int SCAN_STATE_IDLE = 0;
    public static final int SCAN_STATE_SCANNING = 1;

    private int mScanState = SCAN_STATE_IDLE;

    @Inject
    AppDatabase mAppDatabase;
    @Inject
    @Named("user_info")
    protected SharedPreferences mUserInfoSp;
    @Inject
    @Named("setting")
    protected SharedPreferences mSettingSp;

    private final Object mLockObject = new Object();

    private ConcurrentHashMap<String, String> mScanMap = new ConcurrentHashMap<>();

    private Thread mThread;

    @Override
    public void onCreate() {
        super.onCreate();
        ((App) getApplication()).appComponent().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            parseIntent(intent);
            if (!mUserInfoSp.getBoolean("is_media_scanned", false)) {
                mUserInfoSp.edit().putBoolean("is_media_scanned", true).apply();
            }
        }
        Timber.d("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 解析Intent
     */
    private void parseIntent(final Intent intent) {
        final Bundle arguments = intent.getExtras();
        if (arguments != null) {
            if (arguments.containsKey(EXTRA_DIRECTORY)) {
                String directory = arguments.getString(EXTRA_DIRECTORY);
                if (directory != null) {
                    if (!mScanMap.containsKey(directory)) {
                        mScanMap.put(directory, "");
                    }
                }
            }
        } else {
            File[] files = Environment.getExternalStorageList(this);
            if (files != null) {
                for (File file : files)
                    mScanMap.put(file.getAbsolutePath(), "");
            }
        }
        if (mThread == null || !mThread.isAlive()) {
            mThread = new Thread(this);
            mThread.start();
        }
    }

    @Override
    public void run() {
        scan();
    }

    /**
     * 扫描
     */
    private void scan() {
        notifyScanStateChanged(SCAN_STATE_SCANNING);
        checkDeleted();
        while (mScanMap.keySet().size() > 0) {
            String path = "";
            for (String key : mScanMap.keySet()) {
                path = key;
                break;
            }
            if (mScanMap.containsKey(path)) {
                String mimeType = mScanMap.get(path);
                if ("".equals(mimeType)) {
                    scanDirectory(path);
                }
                //扫描完成一个
                mScanMap.remove(path);
            }
        }
        //停止服务
        stopSelf();
        notifyScanStateChanged(SCAN_STATE_IDLE);
    }

    /**
     * 扫描文件夹
     */
    private void scanDirectory(String path) {
        eachAllMedias(new File(path));
    }

    /**
     * 递归查找视频
     */
    private void eachAllMedias(File f) {
        if (f == null) {
            return;
        }
        if (!f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            if (f.getAbsolutePath().startsWith(".")) {
                return;
            }
            File[] innerFiles = f.listFiles();
            if (innerFiles == null) {
                return;
            }
            boolean escape = false;
            if (mSettingSp.getBoolean("filter_no_media", true)) {
                for (File file : innerFiles) {
                    if (file.getAbsolutePath().contains(".nomedia")) {
                        escape = true;
                        break;
                    }
                }
            }
            if (escape) {
                return;
            }
            for (File file : innerFiles) {
                eachAllMedias(file);
            }
        } else {
            if (f.canRead() && MimeUtils.isVideo(f.getPath())) {
                save(f);
            }
        }
    }

    @SuppressLint("CheckResult")
    private void checkDeleted() {
        mAppDatabase.videoLocalDao().getAllAsync()
                .firstOrError()
                .toObservable()
                .flatMap(Observable::fromIterable)
                .toList()
                .subscribe(videoLocals -> {
                    VideoLocal[] a = new VideoLocal[videoLocals.size()];
                    mAppDatabase.videoLocalDao().delete(videoLocals.toArray(a));
                });
    }

    /**
     * 保存入库
     */
    private void save(final File videoFile) {
        synchronized (mLockObject) {
            try {
                IjkMediaPlayer mediaPlayer = new IjkMediaPlayer();
                try {
                    mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
                    mediaPlayer.setDataSource(videoFile.getAbsolutePath());
                    mediaPlayer.setOnPreparedListener(mp -> {
                        synchronized (mLockObject) {
                            List<VideoLocal> videos = mAppDatabase.videoLocalDao().getByDataSync(videoFile.getAbsolutePath());
                            VideoLocal video;
                            if (videos.isEmpty()) {
                                video = new VideoLocal();
                            } else {
                                video = videos.get(0);
                            }
                            video.data = videoFile.getAbsolutePath();
                            video.directory = videoFile.getParent();
                            video.directoryName = FileUtils.getFileName(videoFile.getParent());
                            video.title = videoFile.getName();
                            video.titleKey = videoFile.getName().substring(0, 1);
                            video.size = videoFile.length();
                            video.duration = mp.getDuration();
                            video.width = (long) mp.getVideoWidth();
                            video.height = (long) mp.getVideoHeight();
                            video.dateAdded = new Date(System.currentTimeMillis());
                            video.dateModified = new Date(videoFile.lastModified());
                            video.mimeType = MimeUtils.getMimeType(videoFile.getName());

                            mAppDatabase.videoLocalDao().insert(video);
                            mp.release();
                            mLockObject.notify();
                        }
                    });
                    mediaPlayer.setOnErrorListener((iMediaPlayer, i, i1) -> {
                        synchronized (mLockObject) {
                            mLockObject.notify();
                        }
                        return true;
                    });
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mLockObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        // Unregister all callbacks.
        mCallbacks.kill();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    final RemoteCallbackList<IMediaScannerCallback> mCallbacks = new RemoteCallbackList<>();

    private final IMediaScanner.Stub mBinder = new IMediaScanner.Stub() {

        @Override
        public int getScanState() {
            return mScanState;
        }

        @Override
        public void registerCallback(IMediaScannerCallback cb) {
            if (cb != null) mCallbacks.register(cb);
        }

        @Override
        public void unregisterCallback(IMediaScannerCallback cb) {
            if (cb != null) mCallbacks.unregister(cb);
        }
    };

    private void notifyScanStateChanged(int newState) {
        mScanState = newState;
        // Broadcast to all clients the new value.
        final int N = mCallbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).onScanStateChanged(newState);
            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
                Timber.d(e);
            }
        }
        mCallbacks.finishBroadcast();
    }
}

package com.lcjian.vastplayer.android.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.lcjian.lib.download.Download;
import com.lcjian.lib.download.DownloadListener;
import com.lcjian.lib.download.DownloadManager;
import com.lcjian.lib.download.DownloadStatus;
import com.lcjian.lib.download.SerializablePersistenceAdapter;
import com.lcjian.lib.download.Utils;
import com.lcjian.lib.util.Environment;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.download.DownloadsActivity;

import java.io.File;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DownloadService extends Service {

    private DownloadManager mDownloadManager;
    private DownloadListener mDownloadListener;
    private NotificationUpdater mNotificationUpdater;
    private LocalBinder mLocalBinder;
    private DownloadActionReceiver mDownloadActionReceiver;

    private boolean mStarted;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onCreate() {
        String downloadDirectory = PreferenceManager.getDefaultSharedPreferences(this).getString("download_directory", "");
        if (TextUtils.isEmpty(downloadDirectory)) {
            downloadDirectory = new File(Environment.getExternalStorageList(this)[0], "Download").getAbsolutePath();
        }
        mDownloadManager = new DownloadManager.Builder()
                .defaultDestination(downloadDirectory)
                .persistenceAdapter(new SerializablePersistenceAdapter(getDir("Download", MODE_PRIVATE).getAbsolutePath()))
                .build();
        mDownloadListener = new DownloadListener.SimpleDownloadListener() {

            @Override
            public void onDownloadStatusChanged(Download download, DownloadStatus downloadStatus) {
                checkForeground();
            }
        };
        mDownloadManager.addListener(new DownloadManager.Listener() {

            @Override
            public void onDownloadCreate(Download download) {
                download.addDownloadListener(mDownloadListener);
            }

            @Override
            public void onDownloadDestroy(Download download) {
                download.removeDownloadListener(mDownloadListener);
            }
        });
        mDownloadManager.getDownloadMonitor().start();
        mNotificationUpdater = new NotificationUpdater();
        mLocalBinder = new LocalBinder();
        mDownloadActionReceiver = new DownloadActionReceiver();
        registerReceiver(mDownloadActionReceiver, new IntentFilter(DownloadActionReceiver.ACTION_SHUTDOWN));
        checkForeground();
    }

    @Override
    public void onDestroy() {
        mDownloadManager.shutdown();
        mDownloadManager.getDownloadMonitor().stop();
        mNotificationUpdater.destroy();
        unregisterReceiver(mDownloadActionReceiver);
    }

    private void checkForeground() {
        boolean foreground = false;
        for (Download item : mDownloadManager.getDownloads()) {
            int status = item.getDownloadStatus().getStatus();
            if (status != DownloadStatus.IDLE
                    && status != DownloadStatus.ERROR
                    && status != DownloadStatus.MERGE_ERROR
                    && status != DownloadStatus.COMPLETE) {
                foreground = true;
                break;
            }
        }
        if (foreground) {
            mStarted = true;
            startService(new Intent(this, DownloadService.class));
            startForeground(mNotificationUpdater.notificationId, mNotificationUpdater.notificationBuilder.build());
        } else {
            if (mStarted) {
                mStarted = false;
                stopSelf();
                stopForeground(true);
            }
        }
    }

    public DownloadManager getDownloadManager() {
        return mDownloadManager;
    }

    public class LocalBinder extends Binder {

        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    private class NotificationUpdater {

        private Disposable disposable;
        private NotificationManagerCompat notificationManagerCompat;
        private NotificationCompat.Builder notificationBuilder;
        private int notificationId;

        private NotificationUpdater() {
            notificationManagerCompat = NotificationManagerCompat.from(DownloadService.this);
            notificationBuilder = new NotificationCompat.Builder(DownloadService.this, "Download")
                    .setSmallIcon(R.drawable.ic_launcher_small)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_download))
                    .setContentIntent(PendingIntent.getActivity(DownloadService.this, 1000,
                            new Intent(DownloadService.this, DownloadsActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                    .addAction(new NotificationCompat.Action(
                            R.drawable.ic_shutdown,
                            getString(R.string.shutdown),
                            PendingIntent.getBroadcast(
                                    DownloadService.this,
                                    1001,
                                    new Intent(DownloadActionReceiver.ACTION_SHUTDOWN),
                                    PendingIntent.FLAG_UPDATE_CURRENT)));
            notificationId = new Random().nextInt();

            disposable = Observable
                    .interval(1, TimeUnit.SECONDS)
                    .observeOn(Schedulers.newThread())
                    .subscribe(aLong -> {
                        int totalTaskCount = mDownloadManager.getDownloads().size();
                        Set<Download> monitorDownloads = mDownloadManager.getDownloadMonitor().getDownloads();
                        if (!monitorDownloads.isEmpty()) {
                            long totalDelta = 0;
                            for (Download download : monitorDownloads) {
                                totalDelta += mDownloadManager.getDownloadMonitor().getDownloadDelta(download);
                            }
                            String totalSpeed = Utils.formatBytes(totalDelta, 2) + "/s";
                            notificationBuilder.setContentTitle(getString(R.string.total_task_count, totalTaskCount))
                                    .setContentText(getString(R.string.downloading_task_count, monitorDownloads.size(), totalSpeed));
                            notificationManagerCompat.notify(notificationId, notificationBuilder.build());
                        }
                    }, throwable -> {

                    });
        }

        private void destroy() {
            disposable.dispose();
        }
    }

    private class DownloadActionReceiver extends BroadcastReceiver {

        private static final String ACTION_SHUTDOWN = "intent.action.shutdown";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, ACTION_SHUTDOWN)) {
                mDownloadManager.pauseAll();
            }
        }
    }
}

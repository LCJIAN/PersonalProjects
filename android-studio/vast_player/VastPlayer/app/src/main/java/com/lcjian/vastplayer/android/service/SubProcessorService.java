package com.lcjian.vastplayer.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.lcjian.lib.util.common.FileUtils;
import com.lcjian.lib.util.common.StorageUtils;
import com.lcjian.vastplayer.data.network.Subtitle;
import com.lcjian.vastplayer.data.network.service.SubtitleService;

import org.rauschig.jarchivelib.ArchiveEntry;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class SubProcessorService extends Service {

    public static final int STATE_DETAIL_START = 0;
    public static final int STATE_DETAIL_SUCCESS = 1;
    public static final int STATE_DOWNLOAD_START = 2;
    public static final int STATE_DOWNLOAD_SUCCESS = 3;
    public static final int STATE_EXTRACT_START = 4;
    public static final int STATE_EXTRACT_SUCCESS = 5;
    public static final int STATE_ERROR = 6;
    final RemoteCallbackList<ISubProcessorCallback> mCallbacks = new RemoteCallbackList<>();
    private int mProcessState;
    private final ISubProcessor.Stub mBinder = new ISubProcessor.Stub() {

        @Override
        public int getProcessState() {
            return mProcessState;
        }

        @Override
        public void registerCallback(ISubProcessorCallback cb) {
            if (cb != null) mCallbacks.register(cb);
        }

        @Override
        public void unregisterCallback(ISubProcessorCallback cb) {
            if (cb != null) mCallbacks.unregister(cb);
        }
    };
    private Disposable mDisposable;
    private SubtitleService mSubtitleService;

    @Override
    public void onCreate() {
        super.onCreate();
        mSubtitleService = (new Subtitle()).subtitleService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            subProcess(intent.getIntExtra("sub_id", 0));
        }
        Timber.d("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private void subProcess(int subId) {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        notifyProcessStateChanged(STATE_DETAIL_START);
        mDisposable = mSubtitleService
                .subDetail(subId)
                .flatMap(listSubResponse -> {
                    notifyProcessStateChanged(STATE_DETAIL_SUCCESS);
                    return downloadSubtitle(listSubResponse.sub.subs.get(0).url);
                })
                .flatMap(file -> {
                    notifyProcessStateChanged(STATE_DOWNLOAD_SUCCESS);
                    return extractSubtitle(file);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subtitle -> {
                            notifyProcessStateChanged(STATE_EXTRACT_SUCCESS);
                            notifySubReady(subtitle.getAbsolutePath());
                        },
                        throwable -> {
                            notifyProcessStateChanged(STATE_ERROR);
                            Timber.e(throwable);
                            stopSelf();
                        },
                        this::stopSelf);
    }

    private Observable<File> extractSubtitle(final File subtitleArchive) {
        return Observable.create(emitter -> {
            notifyProcessStateChanged(STATE_EXTRACT_START);
            String fileName = FileUtils.getFileName(subtitleArchive.getAbsolutePath());
            if (isSubtitle(fileName)) {
                emitter.onNext(subtitleArchive);
                emitter.onComplete();
            } else {
                String subtitleDir = subtitleArchive.getParent();
                Archiver archiver = ArchiverFactory.createArchiver(subtitleArchive);
                ArchiveStream stream = null;
                try {
                    stream = archiver.stream(subtitleArchive);
                    ArchiveEntry entry;
                    while ((entry = stream.getNextEntry()) != null) {
                        if (!entry.isDirectory()
                                && isSubtitle(entry.getName())) {
                            File subtitleFile = new File(subtitleDir, entry.getName());
                            if (!subtitleFile.exists()) {
                                entry.extract(new File(subtitleDir));
                            }
                            emitter.onNext(subtitleFile);
                            emitter.onComplete();
                            return;
                        }
                    }
                    emitter.onError(new Exception("No subtitle"));
                } catch (IOException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private boolean isSubtitle(String fileName) {
        return fileName.endsWith(".srt")
                || fileName.endsWith(".ssa")
                || fileName.endsWith(".ass")
                || fileName.endsWith(".sub")
                || fileName.endsWith(".smi");
    }

    private Observable<File> downloadSubtitle(final String url) {
        return Observable.create(emitter -> {
            notifyProcessStateChanged(STATE_DOWNLOAD_START);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            FileOutputStream fos = null;
            InputStream is = null;
            try {
                Response response = (new OkHttpClient()).newCall(request).execute();
                String subFileDir = StorageUtils.getCacheDirectory(SubProcessorService.this).getAbsolutePath() + "/Subtitle/";
                FileUtils.makeDirs(subFileDir);
                File subFile = new File(subFileDir, url.substring(url.lastIndexOf("/"), url.indexOf("?")));
                if (subFile.createNewFile()) {
                    fos = new FileOutputStream(subFile);
                    is = response.body().byteStream();
                    byte[] buffer = new byte[4 * 1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
                emitter.onNext(subFile);
                emitter.onComplete();
            } catch (IOException e) {
                emitter.onError(e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
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

    private void notifyProcessStateChanged(int newState) {
        mProcessState = newState;
        // Broadcast to all clients the new value.
        final int N = mCallbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).onProcessStateChanged(newState);
            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
                Timber.e(e);
            }
        }
        mCallbacks.finishBroadcast();
    }

    private void notifySubReady(String subtitleFile) {
        final int N = mCallbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).onSubReady(subtitleFile);
            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
                Timber.e(e);
            }
        }
        mCallbacks.finishBroadcast();
    }
}

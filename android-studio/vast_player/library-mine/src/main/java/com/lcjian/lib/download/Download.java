package com.lcjian.lib.download;

import com.lcjian.lib.download.exception.ConnectException;
import com.lcjian.lib.download.exception.FileExistsException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public final class Download {

    private final Request request;
    private final Splitter splitter;
    private final DownloadAPI downloadAPI;
    private final RetryPolicy retryPolicy;
    private final PersistenceAdapter persistenceAdapter;
    private final ExecutorService chunkDownloadThreadPool;
    private final String defaultDestination;
    private final CopyOnWriteArrayList<DownloadListener> listeners;
    private final AtomicLong downloadedBytes;
    private final AtomicBoolean pauseFlag = new AtomicBoolean(true);
    private final AtomicBoolean retryFlag = new AtomicBoolean(false);
    private final AtomicBoolean shutdownFlag = new AtomicBoolean(false);
    private final AtomicBoolean deleteFlag = new AtomicBoolean(false);
    private final Semaphore semaphore;
    private final Logger logger;
    private ExecutorService actionThreadPool;
    private List<ChunkDownload> chunkDownloads;
    private DownloadStatus downloadStatus;
    private DownloadInfo downloadInfo;
    private boolean shutdown = false;
    private boolean needRelease = true;

    Download(Request request, DownloadStatus downloadStatus, DownloadInfo downloadInfo, List<ChunkDownload> chunkDownloads,
             String defaultDestination, Splitter splitter, DownloadAPI downloadAPI, RetryPolicy retryPolicy, PersistenceAdapter persistenceAdapter,
             ExecutorService chunkDownloadThreadPool, Semaphore semaphore, Logger logger) {
        this.request = request;
        this.downloadStatus = downloadStatus;
        this.downloadInfo = downloadInfo;
        this.chunkDownloads = chunkDownloads;
        this.splitter = splitter;
        this.downloadAPI = downloadAPI;
        this.retryPolicy = retryPolicy;
        this.persistenceAdapter = persistenceAdapter;
        this.chunkDownloadThreadPool = chunkDownloadThreadPool;
        this.defaultDestination = defaultDestination;
        this.semaphore = semaphore;
        this.logger = logger;
        this.downloadedBytes = new AtomicLong();
        this.listeners = new CopyOnWriteArrayList<>();

        if (this.chunkDownloads == null) {
            this.chunkDownloads = new ArrayList<>();
        } else {
            for (ChunkDownload chunkDownload : this.chunkDownloads) {
                chunkDownload.attach(this);
            }
        }
        int status = this.downloadStatus == null ? DownloadStatus.IDLE : this.downloadStatus.getStatus();
        if (status == DownloadStatus.IDLE
                || status == DownloadStatus.PENDING
                || status == DownloadStatus.INITIALIZING
                || status == DownloadStatus.CHUNK_PENDING
                || status == DownloadStatus.DOWNLOADING
                || status == DownloadStatus.MERGING) {
            this.downloadStatus = new DownloadStatus(DownloadStatus.IDLE);
        }
    }

    public Request getRequest() {
        return request;
    }

    public List<ChunkDownload> getChunkDownloads() {
        return Collections.unmodifiableList(chunkDownloads);
    }

    public File getDownloadFile() {
        return downloadInfo == null ? null : new File(
                Utils.isEmpty(request.destination()) ? defaultDestination : request.destination(),
                Utils.isEmpty(request.fileName()) ? downloadInfo.initInfo().fileName() : request.fileName());
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public long getDownloadedBytes() {
        return downloadedBytes.get();
    }

    public void addDownloadListener(DownloadListener downloadListener) {
        listeners.add(downloadListener);
    }

    public void removeDownloadListener(DownloadListener downloadListener) {
        listeners.remove(downloadListener);
    }

    boolean getPauseFlag() {
        return pauseFlag.get();
    }

    void pauseAsync() {
        if (!pauseFlag.get()) {
            // make this download pause.
            pauseFlag.getAndSet(true);
            List<Runnable> runnableList = actionThreadPool.shutdownNow();
            for (Runnable runnable : runnableList) {
                execute(runnable);
            }
        } else {
            if (!shutdownFlag.get()) {
                logger.warning(Utils.formatString("Download(%s) is paused or pausing.", request.simplifiedId()));
            }
        }
    }

    void resumeAsync() {
        if (pauseFlag.get()) {
            execute(new Runnable() {
                @Override
                public void run() {
                    pauseFlag.getAndSet(false);
                    int st = downloadStatus.getStatus();
                    if (st == DownloadStatus.COMPLETE) {
                        logger.warning(Utils.formatString("Download(%s) is complete.", request.simplifiedId()));
                    } else {
                        if (retryFlag.get()
                                || st == DownloadStatus.IDLE
                                || st == DownloadStatus.ERROR
                                || st == DownloadStatus.MERGE_ERROR) {
                            if (retryFlag.get()) {
                                retryFlag.getAndSet(false);
                            } else {
                                try {
                                    notifyDownloadStatus(new DownloadStatus(DownloadStatus.PENDING));
                                    semaphore.acquire();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    needRelease = false;
                                    notifyDownloadStatus(new DownloadStatus(DownloadStatus.IDLE));
                                    return;
                                }
                            }
                            initialize();
                        } else {
                            logger.warning(Utils.formatString("Download(%s) is running.", request.simplifiedId()));
                        }
                    }
                }
            });
        } else {
            logger.warning(Utils.formatString("Download(%s) is running.", request.simplifiedId()));
        }
    }

    void shutdownAsync(boolean deleteFile) {
        if (!shutdownFlag.get()) {
            // use for winding up when this download is paused.
            shutdownFlag.getAndSet(true);
            deleteFlag.getAndSet(deleteFile);
            pauseAsync();

            // If this download is paused already, we wind up this download directly.
            execute(new Runnable() {
                @Override
                public void run() {
                    int st = downloadStatus.getStatus();
                    if (st == DownloadStatus.IDLE
                            || st == DownloadStatus.ERROR
                            || st == DownloadStatus.MERGE_ERROR
                            || st == DownloadStatus.COMPLETE) {
                        windUp();
                    }
                }
            });
        } else {
            logger.warning(Utils.formatString("Download(%s) is stopped or stopping.", request.simplifiedId()));
        }
    }

    void notifyDownloadProgress(long delta) {
        long bytes = this.downloadedBytes.addAndGet(delta);
        for (DownloadListener downloadListener : listeners) {
            downloadListener.onProgress(this, bytes);
        }
    }

    private void notifyDownloadStatus(DownloadStatus status) {
        int st = status.getStatus();
        boolean changed = st != downloadStatus.getStatus();
        if (changed) {
            if ((st == DownloadStatus.ERROR || st == DownloadStatus.MERGE_ERROR)
                    && retryPolicy.shouldRetry(Download.this, status.getThrowable())
                    && !pauseFlag.get()) {
                retryFlag.getAndSet(true);
            }
            if (st == DownloadStatus.IDLE
                    || st == DownloadStatus.ERROR
                    || st == DownloadStatus.MERGE_ERROR) {
                // make this download resume-able again.
                pauseFlag.getAndSet(true);
            }
            if (retryFlag.get()) {
                for (DownloadListener downloadListener : listeners) {
                    downloadListener.onRetry(Download.this, status.getThrowable());
                }
                logger.info(Utils.formatString("Download(%s) retry.", request.simplifiedId()));
                resumeAsync();
                return;
            }

            downloadStatus = status;
            persistenceAdapter.saveDownloadStatus(request, downloadStatus);
            if (st == DownloadStatus.IDLE
                    || st == DownloadStatus.ERROR
                    || st == DownloadStatus.MERGE_ERROR
                    || st == DownloadStatus.COMPLETE) {
                if (needRelease) {
                    semaphore.release();
                }
                windUp();
            }
            for (DownloadListener downloadListener : listeners) {
                downloadListener.onDownloadStatusChanged(Download.this, downloadStatus);
            }
            logger.fine(Utils.formatString("Download(%s)'s status is changed, status:%d", request.simplifiedId(), downloadStatus.getStatus()));
        }
    }

    void updateDownloadStatusAsync() {
        execute(new Runnable() {
            @Override
            public void run() {
                DownloadStatus tempDownloadStatus = null;
                for (ChunkDownload chunkDownload : chunkDownloads) {
                    if (chunkDownload.getChunkDownloadStatus().getStatus() == ChunkDownloadStatus.DOWNLOADING) {
                        tempDownloadStatus = new DownloadStatus(DownloadStatus.DOWNLOADING);
                        break;
                    }
                }
                if (tempDownloadStatus == null) {
                    for (ChunkDownload chunkDownload : chunkDownloads) {
                        if (chunkDownload.getChunkDownloadStatus().getStatus() == ChunkDownloadStatus.PENDING) {
                            tempDownloadStatus = new DownloadStatus(DownloadStatus.CHUNK_PENDING);
                            break;
                        }
                    }
                }
                if (tempDownloadStatus == null) {
                    for (ChunkDownload chunkDownload : chunkDownloads) {
                        if (chunkDownload.getChunkDownloadStatus().getStatus() == ChunkDownloadStatus.IDLE) {
                            tempDownloadStatus = new DownloadStatus(DownloadStatus.IDLE);
                            break;
                        }
                    }
                }
                if (tempDownloadStatus == null) {
                    for (ChunkDownload chunkDownload : chunkDownloads) {
                        if (chunkDownload.getChunkDownloadStatus().getStatus() == ChunkDownloadStatus.ERROR) {
                            tempDownloadStatus = new DownloadStatus(chunkDownload.getChunkDownloadStatus().getThrowable());
                            break;
                        }
                    }
                }
                if (tempDownloadStatus == null) {
                    merge();
                } else {
                    notifyDownloadStatus(tempDownloadStatus);
                }
            }
        });
    }

    private void execute(Runnable runnable) {
        if (actionThreadPool == null || actionThreadPool.isShutdown()) {
             /*
              * Use only a single thread to keep download status and data correct.
              */
            ThreadPoolExecutor temp = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            temp.allowCoreThreadTimeOut(true);
            actionThreadPool = temp;
        }
        actionThreadPool.execute(runnable);
    }

    private void windUp() {
        if (shutdownFlag.get() && !shutdown) {
            actionThreadPool.shutdown();
            listeners.clear();
            if (deleteFlag.get()) {
                File downloadFile = getDownloadFile();
                if (downloadFile != null && downloadFile.exists()) {
                    if (!downloadFile.delete()) {
                        logger.warning(Utils.formatString("Can not delete download(%s)'s file(%s) when wind up", request.simplifiedId(), downloadFile.getName()));
                    }
                }
                if (chunkDownloads != null && !chunkDownloads.isEmpty()) {
                    for (ChunkDownload chunkDownload : chunkDownloads) {
                        File chunkFile = new File(chunkDownload.getChunk().file());
                        if (chunkFile.exists() && !chunkFile.delete()) {
                            logger.warning(Utils.formatString("Can not delete download(%s)'s chunk file(%s) when wind up", request.simplifiedId(), chunkDownload.getChunk().file()));
                        }
                    }
                    chunkDownloads.clear();
                }
            }
            actionThreadPool = null;
            shutdown = true;
        }
    }

    private void initialize() {
        if (pauseFlag.get()) {
            notifyDownloadStatus(new DownloadStatus(DownloadStatus.IDLE));
            return;
        }
        notifyDownloadStatus(new DownloadStatus(DownloadStatus.INITIALIZING));

        boolean initialized = true;
        if (downloadInfo == null) {
            initialized = false;
        } else {
            if (downloadInfo.initInfo() == null) {
                initialized = false;
            }
        }
        try {
            if (initialized) {
                boolean reInit = !downloadInfo.rangeInfo().rangeSupportable()
                        || downloadAPI.serverFileChanged(request.url(), request.headers(), downloadInfo.initInfo().lastModified());

                if (pauseFlag.get()) {
                    notifyDownloadStatus(new DownloadStatus(DownloadStatus.IDLE));
                    return;
                }

                if (reInit) {
                    downloadInfo = downloadInfo.newBuilder()
                            .initInfo(downloadAPI.getDownloadInitInfo(request.url(), request.headers()))
                            .rangeInfo(downloadAPI.getDownloadRangeInfo(request.url(), request.headers()))
                            .serverFileChanged(true)
                            .build();

                    // remove all chunks and re-split
                    if (chunkDownloads != null && !chunkDownloads.isEmpty()) {
                        for (ChunkDownload chunkDownload : chunkDownloads) {
                            File chunkFile = new File(chunkDownload.getChunk().file());
                            if (chunkFile.exists() && !chunkFile.delete()) {
                                logger.warning(Utils.formatString("Can not delete download(%s)'s chunk file(%s) when re-split.", request.simplifiedId(), chunkDownload.getChunk().file()));
                            }
                        }
                        downloadedBytes.getAndSet(0);
                        chunkDownloads.clear();
                    }
                    splitDownload();
                }
            } else {
                downloadInfo = new DownloadInfo.Builder()
                        .initInfo(downloadAPI.getDownloadInitInfo(request.url(), request.headers()))
                        .rangeInfo(downloadAPI.getDownloadRangeInfo(request.url(), request.headers()))
                        .createTime(System.currentTimeMillis())
                        .serverFileChanged(false)
                        .build();
                // split
                splitDownload();
            }

            if (pauseFlag.get()) {
                notifyDownloadStatus(new DownloadStatus(DownloadStatus.IDLE));
                return;
            }

            for (ChunkDownload chunkDownload : chunkDownloads) {
                ChunkDownload.ChunkDownloader chunkDownloader = chunkDownload.getChunkDownloader();
                chunkDownload.notifyChunkDownloadStatus(new ChunkDownloadStatus(ChunkDownloadStatus.PENDING));
                chunkDownloadThreadPool.execute(chunkDownloader);
            }
        } catch (ConnectException | FileExistsException e) {
            notifyDownloadStatus(new DownloadStatus(e));
        }
    }

    private void splitDownload() throws FileExistsException {
        File downloadFile = getDownloadFile();
        assert downloadFile != null;
        List<Chunk> chunks = splitter.split(
                downloadFile.getAbsolutePath(),
                downloadInfo.initInfo().contentLength(),
                downloadInfo.rangeInfo().rangeSupportable());
        persistenceAdapter.saveDownloadInfo(request, downloadInfo, chunks);
        for (Chunk chunk : chunks) {
            ChunkDownload chunkDownload = new ChunkDownload(request, chunk, null, downloadAPI, persistenceAdapter, logger);
            chunkDownload.attach(Download.this);
            chunkDownloads.add(chunkDownload);
        }
        if (downloadFile.exists()) {
            throw new FileExistsException(downloadFile);
        }
//            if (downloadFile.getUsableSpace() < downloadInfo.initInfo().contentLength()) {
//                throw new RuntimeException("Insufficient disk space");
//            }
    }

    private void merge() {
        notifyDownloadStatus(new DownloadStatus(DownloadStatus.MERGING));

        List<File> parts = new ArrayList<>();
        for (ChunkDownload chunkDownload : chunkDownloads) {
            parts.add(new File(chunkDownload.getChunk().file()));
        }
        File outFile = getDownloadFile();
        assert outFile != null;
        if (outFile.exists()) {
            notifyDownloadStatus(new DownloadStatus(DownloadStatus.MERGE_ERROR, new FileExistsException(outFile)));
            return;
        }
        File partFirst = parts.get(0);
        partFirst.renameTo(outFile);
        parts.remove(0);
        FileOutputStream os = null;
        FileChannel outFileChannel = null;
        try {
            os = new FileOutputStream(outFile, true);
            outFileChannel = os.getChannel();
            for (File part : parts) {
                FileInputStream is = null;
                FileChannel inPartFileChannel = null;
                try {
                    is = new FileInputStream(part);
                    inPartFileChannel = is.getChannel();
                    inPartFileChannel.transferTo(0, inPartFileChannel.size(), outFileChannel);
                } catch (IOException e) {
                    notifyDownloadStatus(new DownloadStatus(DownloadStatus.MERGE_ERROR, e));
                    return;
                } finally {
                    if (inPartFileChannel != null) {
                        try {
                            inPartFileChannel.close();
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
                if (!part.delete()) {
                    logger.warning(Utils.formatString("Can not delete download(%s)'s chunk file(%s) when merging.", request.simplifiedId(), part.getName()));
                }
            }
            notifyDownloadStatus(new DownloadStatus(DownloadStatus.COMPLETE));
        } catch (FileNotFoundException e) {
            notifyDownloadStatus(new DownloadStatus(DownloadStatus.MERGE_ERROR, e));
        } finally {
            if (outFileChannel != null) {
                try {
                    outFileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

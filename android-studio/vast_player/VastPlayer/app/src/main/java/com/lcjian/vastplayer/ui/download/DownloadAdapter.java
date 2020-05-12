package com.lcjian.vastplayer.ui.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.lcjian.lib.download.Download;
import com.lcjian.lib.download.DownloadInfo;
import com.lcjian.lib.download.DownloadManager;
import com.lcjian.lib.download.DownloadMonitor;
import com.lcjian.lib.download.DownloadStatus;
import com.lcjian.lib.download.Utils;
import com.lcjian.lib.download.exception.FileExistsException;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.lib.util.common.FileUtils;
import com.lcjian.lib.widget.CircleProgressBar;
import com.lcjian.vastplayer.Global;
import com.lcjian.vastplayer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> {

    private List<Download> mData;

    private DownloadManager mDownloadManager;

    private ActionMode mActionMode;

    private ActionMode.Callback mCallback;

    private boolean mInActionMode;

    private List<Download> mChecked;

    private ViewGroup mParent;

    private CompositeDisposable mDisposables;

    DownloadAdapter(List<Download> data, DownloadManager downloadManager) {
        this.mData = data;
        this.mDownloadManager = downloadManager;
        this.mDisposables = new CompositeDisposable();
        this.mChecked = new ArrayList<>();
        this.mCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mActionMode = mode;
                mActionMode.setTitle(R.string.action_downloads);
                mActionMode.setSubtitle(mParent.getContext().getString(R.string.items_count_selected, mChecked.size()));
                mode.getMenuInflater().inflate(R.menu.menu_delete, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                mInActionMode = true;
                TransitionManager.beginDelayedTransition(mParent);
                notifyItemRangeChanged(0, getItemCount(), mInActionMode);
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    deleteCheckedDownloads();
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mInActionMode = false;
                mChecked.clear();
                TransitionManager.beginDelayedTransition(mParent);
                notifyItemRangeChanged(0, getItemCount(), mInActionMode);
            }
        };
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DownloadViewHolder(parent, mDownloadManager, mDisposables, this);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
        holder.bindTo(mData.get(position));
        holder.startUpdateInterval();
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            holder.updateActionMode();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull DownloadViewHolder holder) {
        holder.stopUpdateInterval();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mParent = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mParent = null;
    }

    void destroy() {
        mDisposables.dispose();
    }

    private void deleteCheckedDownloads() {
        for (Download download : mChecked) {
            int status = download.getDownloadStatus().getStatus();
            mDownloadManager.delete(download,
                    !(status == DownloadStatus.COMPLETE
                            || (status == DownloadStatus.ERROR && download.getDownloadStatus().getThrowable() instanceof FileExistsException)
                            || (status == DownloadStatus.MERGE_ERROR && download.getDownloadStatus().getThrowable() instanceof FileExistsException)));
        }
        mChecked.clear();
    }

    static class DownloadViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_download_mime)
        ImageView iv_download_mime;
        @BindView(R.id.chb_download)
        CheckBox chb_download;
        @BindView(R.id.tv_download_title)
        TextView tv_download_title;
        @BindView(R.id.tv_download_detail)
        TextView tv_download_detail;
        @BindView(R.id.fl_download_progress)
        FrameLayout fl_download_progress;
        @BindView(R.id.pb_download_progress)
        CircleProgressBar pb_download_progress;
        @BindView(R.id.iv_pause)
        ImageView iv_pause;
        @BindView(R.id.tv_download_progress)
        TextView tv_download_progress;

        DownloadManager downloadManager;
        DownloadMonitor downloadMonitor;
        Download download;
        StringBuilder detailStrBuilder;
        CompositeDisposable disposables;
        Disposable disposable;

        private DownloadAdapter mAdapter;

        DownloadViewHolder(ViewGroup parent, DownloadManager dm, CompositeDisposable cs, DownloadAdapter adapter) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.download_item, parent, false));
            ButterKnife.bind(this, this.itemView);
            this.downloadManager = dm;
            this.downloadMonitor = dm.getDownloadMonitor();
            this.detailStrBuilder = new StringBuilder();
            this.disposables = cs;
            this.mAdapter = adapter;
            fl_download_progress.setOnClickListener(v -> {
                int status = download.getDownloadStatus().getStatus();
                if (status != DownloadStatus.COMPLETE) {
                    if (status == DownloadStatus.IDLE
                            || status == DownloadStatus.ERROR
                            || status == DownloadStatus.MERGE_ERROR) {
                        downloadManager.resume(download.getRequest());
                    } else {
                        downloadManager.pause(download.getRequest());
                    }
                }
            });
            itemView.setOnClickListener(view -> {
                if (mAdapter.mInActionMode) {
                    checkDownload();
                } else {
                    if (download.getDownloadFile() != null) {
                        Intent intent = Intent.createChooser(
                                new Intent()
                                        .setAction(Intent.ACTION_GET_CONTENT)
                                        .addCategory(Intent.CATEGORY_OPENABLE)
                                        .setDataAndType(Uri.fromFile(download.getDownloadFile().getParentFile()), "*/*"),
                                "Open download directory");
                        if (view.getContext().getPackageManager().resolveActivity(intent, 0) != null) {
                            view.getContext().startActivity(intent);
                        }
                    }
                }
            });
            itemView.setOnLongClickListener(v -> {
                ((AppCompatActivity) v.getContext()).startSupportActionMode(mAdapter.mCallback);
                checkDownload();
                return true;
            });
        }

        void bindTo(Download d) {
            this.download = d;
            update();
        }

        void updateActionMode() {
            iv_download_mime.setVisibility(mAdapter.mInActionMode ? View.GONE : View.VISIBLE);
            chb_download.setVisibility(mAdapter.mInActionMode ? View.VISIBLE : View.GONE);
            chb_download.setChecked(mAdapter.mChecked.contains(download));
            itemView.setActivated(mAdapter.mChecked.contains(download) && mAdapter.mInActionMode);
            itemView.setLongClickable(!mAdapter.mInActionMode);
        }

        private void checkDownload() {
            if (mAdapter.mChecked.contains(download)) {
                mAdapter.mChecked.remove(download);
            } else {
                mAdapter.mChecked.add(download);
            }
            if (mAdapter.mActionMode != null) {
                mAdapter.mActionMode.setSubtitle(itemView.getContext().getString(R.string.items_count_selected, mAdapter.mChecked.size()));
            }
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            mAdapter.notifyItemChanged(getAdapterPosition(), mAdapter.mInActionMode);
        }

        void startUpdateInterval() {
            stopUpdateInterval();
            if (download.getDownloadStatus().getStatus() == DownloadStatus.DOWNLOADING) {
                disposable = Observable.interval(1, 1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            if (download.getDownloadStatus().getStatus() == DownloadStatus.DOWNLOADING) {
                                update();
                            } else {
                                stopUpdateInterval();
                            }
                        }, throwable -> {
                        });
                disposables.add(disposable);
            }
        }

        void stopUpdateInterval() {
            if (disposable != null) {
                disposable.dispose();
                disposables.remove(disposable);
                disposable = null;
            }
        }

        private void update() {
            Context context = itemView.getContext();
            DownloadInfo downloadInfo = download.getDownloadInfo();
            File downloadFile = download.getDownloadFile();
            long contentLength = downloadInfo == null ? 0 : downloadInfo.initInfo().contentLength();
            int status = download.getDownloadStatus().getStatus();
            long downloadedBytes = download.getDownloadedBytes();
            long downloadDelta = downloadMonitor.getDownloadDelta(download);
            long downloadEstimatedTime = downloadMonitor.getDownloadEstimatedTime(download);

            String mimeType = downloadInfo == null ? "" : downloadInfo.initInfo().mimeType();
            if (!TextUtils.isEmpty(mimeType)) {
                mimeType = mimeType.replace("/", "-");
            }
            String title = downloadFile == null ? download.getRequest().simplifiedId() : FileUtils.getFileName(downloadFile.getAbsolutePath());
            String statusStr;
            switch (status) {
                case DownloadStatus.IDLE:
                    statusStr = context.getString(R.string.download_status_idle);
                    break;
                case DownloadStatus.PENDING:
                    statusStr = context.getString(R.string.download_status_pending);
                    break;
                case DownloadStatus.INITIALIZING:
                    statusStr = context.getString(R.string.download_status_initializing);
                    break;
                case DownloadStatus.CHUNK_PENDING:
                    statusStr = context.getString(R.string.download_status_chunk_pending);
                    break;
                case DownloadStatus.DOWNLOADING:
                    statusStr = context.getString(R.string.download_status_downloading);
                    break;
                case DownloadStatus.ERROR:
                    if (download.getDownloadStatus().getThrowable() instanceof FileExistsException) {
                        statusStr = context.getString(R.string.download_status_error_file_exist);
                    } else {
                        statusStr = context.getString(R.string.download_status_error);
                    }
                    break;
                case DownloadStatus.MERGING:
                    statusStr = context.getString(R.string.download_status_merging);
                    break;
                case DownloadStatus.MERGE_ERROR:
                    if (download.getDownloadStatus().getThrowable() instanceof FileExistsException) {
                        statusStr = context.getString(R.string.download_status_error_file_exist);
                    } else {
                        statusStr = context.getString(R.string.download_status_merge_error);
                    }
                    break;
                case DownloadStatus.COMPLETE:
                    statusStr = context.getString(R.string.download_status_complete);
                    break;
                default:
                    statusStr = context.getString(R.string.download_status_idle);
                    break;
            }
            String sizeStr = (status == DownloadStatus.COMPLETE ? "" : Utils.formatBytes(downloadedBytes, 2) + "/")
                    + (status == DownloadStatus.COMPLETE
                    ? (downloadFile != null && downloadFile.exists() ? Utils.formatBytes(downloadFile.length(), 2) : context.getString(R.string.file_is_deleted))
                    : (contentLength == 0 || contentLength == -1 ? context.getString(R.string.unknown) : Utils.formatBytes(contentLength, 2)));
            String percentStr = contentLength == 0 || contentLength == -1 ? context.getString(R.string.unknown) : Utils.formatPercent(downloadedBytes / (float) contentLength);
            String deltaStr = (status != DownloadStatus.DOWNLOADING || downloadDelta == -1 ? "" : Utils.formatBytes(downloadDelta, 2) + "/s");
            String estimatedTimeStr = (status != DownloadStatus.DOWNLOADING || downloadEstimatedTime == -1 ? "" : DateUtils.stringForTime(downloadEstimatedTime));
            if (!TextUtils.isEmpty(estimatedTimeStr) && estimatedTimeStr.length() > 10) {
                estimatedTimeStr = context.getString(R.string.unknown);
            }
            detailStrBuilder.setLength(0);
            detailStrBuilder.append(statusStr)
                    .append((TextUtils.isEmpty(estimatedTimeStr) ? "" : " • ")).append(estimatedTimeStr).append("\n")
                    .append(sizeStr)
                    .append((TextUtils.isEmpty(sizeStr) || TextUtils.isEmpty(deltaStr) ? "" : " • ")).append(deltaStr);

            Glide.with(itemView)
                    .load("https://mimetypeicons.ga/mimetypes-icons/64/" + mimeType + ".png")
                    .apply(Global.centerCrop)
                    .transition(Global.dontTransition)
                    .into(iv_download_mime);
            tv_download_title.setText(title);
            tv_download_detail.setText(detailStrBuilder);

            if (status == DownloadStatus.COMPLETE) {
                tv_download_progress.setText(null);
                VectorDrawableCompat drawable = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_complete_white_24dp, context.getTheme());
                if (drawable != null) {
                    drawable.setTint(ContextCompat.getColor(context, R.color.accent));
                }
                tv_download_progress.setBackgroundDrawable(drawable);
                iv_pause.setVisibility(View.GONE);

                pb_download_progress.setPercent(100);
            } else {
                tv_download_progress.setText(percentStr);
                tv_download_progress.setBackgroundDrawable(null);
                iv_pause.setVisibility(View.VISIBLE);
                if (status == DownloadStatus.IDLE
                        || status == DownloadStatus.ERROR
                        || status == DownloadStatus.MERGE_ERROR) {
                    iv_pause.setImageResource(R.drawable.ic_play_black_24dp);
                } else {
                    iv_pause.setImageResource(R.drawable.ic_pause_black_24dp);
                }

                if (status == DownloadStatus.PENDING
                        || status == DownloadStatus.INITIALIZING
                        || status == DownloadStatus.CHUNK_PENDING
                        || status == DownloadStatus.MERGING
                        || (status == DownloadStatus.DOWNLOADING && TextUtils.equals(percentStr, context.getString(R.string.unknown)))) {
                    pb_download_progress.setIndeterminate(true);
                } else {
                    if (TextUtils.equals(percentStr, context.getString(R.string.unknown))) {
                        pb_download_progress.setPercent(0);
                    } else {
                        pb_download_progress.setPercent(Float.parseFloat(percentStr.replace("%", "")));
                    }
                }
            }

            updateActionMode();
        }
    }
}
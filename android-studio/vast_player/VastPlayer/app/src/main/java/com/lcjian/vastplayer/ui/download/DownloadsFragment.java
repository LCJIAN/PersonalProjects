package com.lcjian.vastplayer.ui.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StatFs;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.lcjian.lib.download.Download;
import com.lcjian.lib.download.DownloadListener;
import com.lcjian.lib.download.DownloadManager;
import com.lcjian.lib.download.DownloadStatus;
import com.lcjian.lib.download.Request;
import com.lcjian.lib.download.Utils;
import com.lcjian.lib.util.Environment;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.android.service.DownloadService;
import com.lcjian.vastplayer.ui.base.BaseFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class DownloadsFragment extends BaseFragment {

    @BindView(R.id.v_download_header)
    View mHeaderView;
    @BindView(R.id.rv_downloads)
    RecyclerView rv_downloads;
    Unbinder unbinder;
    private CompositeDisposable mSubscriptions;

    private DownloadAdapter mAdapter;
    private DownloadManager mDownloadManager;
    private List<Download> mDownloads = new ArrayList<>();
    private DownloadListener mDownloadListener = new DownloadListener.SimpleDownloadListener() {

        @Override
        public void onDownloadStatusChanged(Download download, DownloadStatus downloadStatus) {
            mRxBus.send(new Event("update_download_status", download));
        }

    };

    private DownloadManager.Listener mListener = new DownloadManager.Listener() {

        @Override
        public void onDownloadCreate(Download download) {
            download.addDownloadListener(mDownloadListener);
            mRxBus.send(new Event("add_download", download));
        }

        @Override
        public void onDownloadDestroy(Download download) {
            download.removeDownloadListener(mDownloadListener);
            mRxBus.send(new Event("remove_download", download));
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService downloadService = ((DownloadService.LocalBinder) service).getService();
            mDownloadManager = downloadService.getDownloadManager();
            mDownloadManager.addListener(mListener);
            for (Download download : mDownloadManager.getDownloads()) {
                download.addDownloadListener(mDownloadListener);
            }

            mDownloads.clear();
            mDownloads.addAll(mDownloadManager.getDownloads());
            mAdapter = new DownloadAdapter(mDownloads, mDownloadManager);
            rv_downloads.setAdapter(mAdapter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDownloadManager.removeListener(mListener);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloads, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv_downloads.setLayoutManager(new LinearLayoutManager(getActivity()));

        mSubscriptions = new CompositeDisposable();
        mSubscriptions.add(mRxBus.asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event instanceof Event) {
                        String action = ((Event) event).action;
                        Download download = ((Event) event).download;
                        int position = mDownloads.indexOf(download);
                        if (TextUtils.equals(action, "update_download_status")) {
                            mAdapter.notifyItemChanged(position);
                        } else if (TextUtils.equals(action, "add_download")) {
                            mDownloads.add(download);
                            mAdapter.notifyItemInserted(mDownloads.indexOf(download));
                        } else if (TextUtils.equals(action, "remove_download")) {
                            mDownloads.remove(download);
                            mAdapter.notifyItemRemoved(position);
                        }
                    } else if (event.toString().startsWith("http://")
                            || event.toString().startsWith("https://")) {
                        mDownloadManager.enqueue(new Request.Builder().url(event.toString()).build());
                    }
                }, throwable -> {
                }));
        if (getActivity() != null) {
            getActivity().bindService(new Intent(getActivity(), DownloadService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroyView() {
        if (getActivity() != null) {
            getActivity().unbindService(mServiceConnection);
        }
        mAdapter.destroy();
        mDownloadManager.removeListener(mListener);
        for (Download download : mDownloadManager.getDownloads()) {
            download.removeDownloadListener(mDownloadListener);
        }
        if (mSubscriptions != null) {
            mSubscriptions.dispose();
        }
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_download, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                if (getFragmentManager() != null) {
                    new DownloadAddTaskFragment().show(getFragmentManager(), "DownloadAddTaskFragment");
                }
                return true;
            case R.id.action_disk_info:
                TransitionManager.beginDelayedTransition((ViewGroup) mHeaderView.getParent());
                if (mHeaderView.getVisibility() == View.VISIBLE) {
                    mHeaderView.setVisibility(View.GONE);
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        TextView tv_downloaded_bytes = mHeaderView.findViewById(R.id.tv_downloaded_bytes);
                        ProgressBar pb_bytes_info = mHeaderView.findViewById(R.id.pb_bytes_info);
                        TextView tv_other_bytes = mHeaderView.findViewById(R.id.tv_other_bytes);
                        TextView tv_available_bytes = mHeaderView.findViewById(R.id.tv_available_bytes);

                        String downloadDirectory = mSettingSp.getString("download_directory", "");
                        if (TextUtils.isEmpty(downloadDirectory)) {
                            downloadDirectory = new File(Environment.getExternalStorageList(getActivity())[0], "Download").getAbsolutePath();
                        }
                        StatFs statFs = new StatFs(downloadDirectory);

                        long downloadedBytes = 0;
                        for (Download download : mDownloads) {
                            File file = download.getDownloadFile();
                            downloadedBytes += download.getDownloadStatus().getStatus() == DownloadStatus.COMPLETE && file != null ?
                                    file.length() : download.getDownloadedBytes();
                        }
                        long totalBytes = statFs.getTotalBytes();
                        long availableBytes = statFs.getAvailableBytes();
                        long usedBytes = totalBytes - availableBytes;
                        long otherBytes = usedBytes - downloadedBytes;

                        tv_downloaded_bytes.setText(getString(R.string.bytes_downloaded, Utils.formatBytes(downloadedBytes, 2)));
                        tv_other_bytes.setText(getString(R.string.bytes_other, Utils.formatBytes(otherBytes, 2)));
                        tv_available_bytes.setText(getString(R.string.bytes_available, Utils.formatBytes(availableBytes, 2)));

                        pb_bytes_info.setProgress((int) (otherBytes * 100f / statFs.getTotalBytes()));
                        pb_bytes_info.setSecondaryProgress((int) (usedBytes * 100f / statFs.getTotalBytes()));
                    }
                    mHeaderView.setVisibility(View.VISIBLE);
                }
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class Event {
        private String action;
        private Download download;

        private Event(String action, Download download) {
            this.action = action;
            this.download = download;
        }
    }
}

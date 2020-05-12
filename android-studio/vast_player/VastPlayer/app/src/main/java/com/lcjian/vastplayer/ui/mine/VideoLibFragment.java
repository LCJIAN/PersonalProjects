package com.lcjian.vastplayer.ui.mine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lcjian.lib.recyclerview.EmptyAdapter;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.android.service.IMediaScanner;
import com.lcjian.vastplayer.android.service.IMediaScannerCallback;
import com.lcjian.vastplayer.android.service.MediaScannerService;
import com.lcjian.vastplayer.data.db.entity.VideoLocal;
import com.lcjian.vastplayer.data.entity.VideoGroup;
import com.lcjian.vastplayer.ui.base.BaseFragment;
import com.lcjian.vastplayer.ui.download.DownloadsActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class VideoLibFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.srl_video_lib)
    SwipeRefreshLayout srl_video_lib;
    @BindView(R.id.rv_video_lib)
    RecyclerView rv_video_lib;

    private Subject<Integer> mSubject = PublishSubject.create();

    private Integer mCurrentGroupType = 0;

    private Unbinder mUnBinder;

    private VideoLibAdapter mAdapter;

    private GridLayoutManager mGridLayoutManager;

    private Disposable mDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_lib, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mCurrentGroupType = 0;
        toolbar.setTitle(R.string.action_video_library);
        mRxBus.send(toolbar);
        srl_video_lib.setColorSchemeResources(R.color.primary);
        srl_video_lib.setOnRefreshListener(this);

        mGridLayoutManager = new GridLayoutManager(getActivity(), 1);
        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) {
                if (position == RecyclerView.NO_POSITION) {
                    return 1;
                }
                int spanCount = mGridLayoutManager.getSpanCount();
                if (spanCount == 1) {
                    return 1;
                } else {
                    List<Object> data = mAdapter.getData();
                    if (position >= data.size()) {
                        return spanCount;
                    }
                    Object displayable = data.get(position);
                    if (displayable instanceof VideoLocal) {
                        if (((VideoLocal) displayable).duration >= 30 * 60 * 1000) {
                            int index = 0;
                            int count = 1;
                            int prePosition = position - 1;
                            while (true) {
                                if (prePosition == -1
                                        || data.get(prePosition) instanceof VideoGroup
                                        || (data.get(prePosition) instanceof VideoLocal
                                        && ((VideoLocal) data.get(prePosition)).duration < 30 * 60 * 1000)) {
                                    break;
                                }
                                prePosition--;
                                index++;
                                count++;
                            }
                            int nextPosition = position + 1;
                            while (true) {
                                if (nextPosition == data.size()
                                        || data.get(nextPosition) instanceof VideoGroup
                                        || (data.get(nextPosition) instanceof VideoLocal
                                        && ((VideoLocal) data.get(nextPosition)).duration < 30 * 60 * 1000)) {
                                    break;
                                }
                                nextPosition++;
                                count++;
                            }
                            if (count % 2 == 0) {
                                return 3;
                            } else {
                                if (index == count - 1) {
                                    return spanCount;
                                } else {
                                    return 3;
                                }
                            }
                        } else {
                            return 2;
                        }
                    } else {
                        return spanCount;
                    }
                }
            }
        };
        spanSizeLookup.setSpanIndexCacheEnabled(true);
        mGridLayoutManager.setSpanSizeLookup(spanSizeLookup);
        rv_video_lib.setLayoutManager(mGridLayoutManager);
        rv_video_lib.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (mGridLayoutManager.getItemViewType(view) == 1) {
                    int position = parent.getChildAdapterPosition(view);
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    int spanCount = mGridLayoutManager.getSpanCount();
                    int spanSize = mGridLayoutManager.getSpanSizeLookup().getSpanSize(position);
                    int spanIndex = mGridLayoutManager.getSpanSizeLookup().getSpanIndex(position, spanCount);
                    int spanGroupIndex = mGridLayoutManager.getSpanSizeLookup().getSpanGroupIndex(position, spanCount);
                    int nextSpanGroupIndex = spanGroupIndex + 1;

                    float space = DimenUtils.dipToPixels(4, getActivity());

                    int left = 0;
                    int right = 0;
                    int bottom = 0;
                    if (spanSize == 2) {
                        if (spanIndex == 0) {
                            right = (int) (space / 2);
                        }
                        if (spanIndex == 2) {
                            left = (int) (space / 2);
                            right = (int) (space / 2);
                        }
                        if (spanIndex == 2 || spanIndex == 4) {
                            left = (int) (space / 2);
                        }
                    }
                    if (spanSize == 3) {
                        if (spanIndex == 0) {
                            right = (int) (space / 2);
                        }
                        if (spanIndex == 3) {
                            left = (int) (space / 2);
                        }
                    }
                    int nextPosition = position + 1;
                    while (true) {
                        if (nextPosition == parent.getAdapter().getItemCount()) {
                            break;
                        }
                        if (nextPosition < parent.getAdapter().getItemCount()
                                && mGridLayoutManager.getSpanSizeLookup().getSpanGroupIndex(nextPosition, spanCount) == nextSpanGroupIndex) {
                            if (parent.getAdapter().getItemViewType(nextPosition) == 1) {
                                bottom = (int) space;
                            }
                            break;
                        }
                        nextPosition++;
                    }
                    outRect.set(left, 0, right, bottom);
                } else {
                    super.getItemOffsets(outRect, view, parent, state);
                }
            }
        });
        mAdapter = new VideoLibAdapter(new ArrayList<>(), mAppDatabase, mGridLayoutManager);
        EmptyAdapter emptyAdapter = new EmptyAdapter(mAdapter).setEmptyView(LayoutInflater.from(getActivity()).inflate(R.layout.empty_data, srl_video_lib, false));
        emptyAdapter.setHasStableIds(true);
        rv_video_lib.setAdapter(emptyAdapter);

        mDisposable = Observable.combineLatest(
                mAppDatabase
                        .videoLocalDao()
                        .getAllAsync().toObservable(),
                mSubject.as(upstream -> upstream),
                (videos, integer) -> {
                    List<VideoGroup> videoGroups = new ArrayList<>();
                    for (VideoLocal video : videos) {
                        VideoGroup videoGroup = null;
                        for (VideoGroup item : videoGroups) {
                            if (integer == 0) {
                                if (TextUtils.equals(item.name, video.directory)) {
                                    videoGroup = item;
                                    break;
                                }
                            } else if (integer == 1) {
                                if (DateUtils.isSameDay(video.dateModified,
                                        DateUtils.convertStrToDate(item.name, "EEEE MMM dd yyyy"))) {
                                    videoGroup = item;
                                    break;
                                }
                            } else if (integer == 2) {
                                if (DateUtils.isSameMonth(video.dateModified,
                                        DateUtils.convertStrToDate(item.name, "MMMM yyyy"))) {
                                    videoGroup = item;
                                    break;
                                }
                            }
                        }
                        if (videoGroup == null) {
                            videoGroup = new VideoGroup();
                            videoGroup.name = "";
                            if (integer == 0) {
                                videoGroup.name = video.directory;
                            } else if (integer == 1) {
                                videoGroup.name = DateUtils.convertDateToStr(video.dateModified, "EEEE MMM dd yyyy");
                            } else if (integer == 2) {
                                videoGroup.name = DateUtils.convertDateToStr(video.dateModified, "MMMM yyyy");
                            }
                            videoGroup.localVideos = new ArrayList<>();
                            videoGroups.add(videoGroup);
                        }

                        videoGroup.localVideos.add(video);
                    }
                    if (integer == 0) {
                        Collections.sort(videoGroups, (o1, o2) -> (TextUtils.isEmpty(o1.name) ? "0" : o1.name).compareTo(TextUtils.isEmpty(o2.name) ? "0" : o2.name));
                    } else if (integer == 1 || integer == 2) {
                        Collections.sort(videoGroups, (o1, o2) -> DateUtils.isBefore(o1.localVideos.get(0).dateModified, o2.localVideos.get(0).dateModified) ? 1 : -1);
                    }
                    return videoGroups;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        videoGroups -> {
                            List<Object> displayable = new ArrayList<>();
                            for (VideoGroup videoGroup : videoGroups) {
                                displayable.add(videoGroup);
                                displayable.addAll(videoGroup.localVideos);
                            }
                            mAdapter.replaceAll(displayable);
                        },
                        throwable -> {
                        });
        mSubject.onNext(mCurrentGroupType);

        mHandler = new MediaScannerHandler(this);

        getActivity().bindService(new Intent(getActivity(), MediaScannerService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        if (!mUserInfoSp.getBoolean("is_media_scanned", false)) {
            getActivity().startService(new Intent(getActivity(), MediaScannerService.class));
        }
    }

    @Override
    public void onRefresh() {
        getActivity().startService(new Intent(getActivity(), MediaScannerService.class));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_video_lib, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_view_type: {
                if (mGridLayoutManager.getSpanCount() == 1) {
                    item.setIcon(R.drawable.ic_grid_white_24dp);
                    item.setTitle(R.string.action_grid_view);
                    mGridLayoutManager.setSpanCount(6);
                } else {
                    item.setIcon(R.drawable.ic_list_white_24dp);
                    item.setTitle(R.string.action_list_view);
                    mGridLayoutManager.setSpanCount(1);
                }
                mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());
                return true;
            }
            case R.id.action_group_type: {
                int groupType = ++mCurrentGroupType % 3;
                item.setIcon(groupType == 0
                        ? R.drawable.ic_folder_white_24dp
                        : (groupType == 1 ? R.drawable.ic_today_white_24dp : R.drawable.ic_date_range_white_24dp));
                item.setTitle(groupType == 0
                        ? R.string.action_group_directory
                        : (groupType == 1 ? R.string.action_group_day : R.string.action_group_month));
                mSubject.onNext(groupType);
                return true;
            }
            case R.id.action_downloads: {
                startActivity(new Intent(getContext(), DownloadsActivity.class));
                return true;
            }
            case R.id.action_settings: {
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        try {
            if (mMediaScanner != null) {
                mMediaScanner.unregisterCallback(mMediaScannerCallback);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mMediaScanner = null;
        getActivity().unbindService(mServiceConnection);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mUnBinder.unbind();
        super.onDestroyView();
    }

    private IMediaScanner mMediaScanner;

    private Handler mHandler;

    private IMediaScannerCallback mMediaScannerCallback = new IMediaScannerCallback.Stub() {

        @Override
        public void onScanStateChanged(int newState) {
            mHandler.sendEmptyMessage(newState);
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMediaScanner = IMediaScanner.Stub.asInterface(service);
            int scanState = MediaScannerService.SCAN_STATE_IDLE;
            try {
                scanState = mMediaScanner.getScanState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mHandler.sendEmptyMessage(scanState);
            try {
                mMediaScanner.registerCallback(mMediaScannerCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                mMediaScanner.unregisterCallback(mMediaScannerCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mMediaScanner = null;
        }
    };

    private static class MediaScannerHandler extends Handler {
        private WeakReference<VideoLibFragment> mVideoLibListFragment;

        MediaScannerHandler(VideoLibFragment videoLibFragment) {
            mVideoLibListFragment = new WeakReference<>(videoLibFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoLibFragment videoLibFragment = mVideoLibListFragment.get();
            if (videoLibFragment != null && videoLibFragment.srl_video_lib != null) {
                if (msg.what == MediaScannerService.SCAN_STATE_IDLE) {
                    videoLibFragment.srl_video_lib.setRefreshing(false);
                } else {
                    videoLibFragment.srl_video_lib.setRefreshing(true);
                }
            }
        }
    }
}

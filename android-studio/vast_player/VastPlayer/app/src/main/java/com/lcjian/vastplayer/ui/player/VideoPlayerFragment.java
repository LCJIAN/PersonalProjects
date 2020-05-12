package com.lcjian.vastplayer.ui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.lcjian.lib.content.ConnectivityChangeHelper;
import com.lcjian.lib.media.IMediaPlayer;
import com.lcjian.lib.media.design.GestureMediaController;
import com.lcjian.lib.media.design.MediaController;
import com.lcjian.lib.media.design.TransportMediator;
import com.lcjian.lib.media.design.VideoView;
import com.lcjian.lib.ui.systemuivis.SystemUiHelper;
import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.RxBus;
import com.lcjian.vastplayer.Utils;
import com.lcjian.vastplayer.android.service.ISubProcessor;
import com.lcjian.vastplayer.android.service.ISubProcessorCallback;
import com.lcjian.vastplayer.android.service.SubProcessorService;
import com.lcjian.vastplayer.data.db.entity.WatchHistory;
import com.lcjian.vastplayer.data.network.entity.Sub;
import com.lcjian.vastplayer.data.network.entity.VideoUrl;
import com.lcjian.vastplayer.ui.base.BaseFragment;
import com.qq.e.ads.interstitial.AbstractInterstitialADListener;
import com.qq.e.ads.interstitial.InterstitialAD;
import com.qq.e.comm.util.AdError;
import com.tapadoo.alerter.Alerter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;

public class VideoPlayerFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.video_view)
    VideoView video_view;
    @BindView(R.id.tv_subtitle)
    TextView tv_subtitle;
    @BindView(R.id.media_controller)
    MediaController media_controller;
    @BindView(R.id.btn_scale)
    ImageButton btn_scale;
    @BindView(R.id.btn_subs)
    ImageButton btn_subs;
    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;
    @BindView(R.id.download_rate)
    TextView download_rate;
    @BindView(R.id.buffer_percent)
    TextView buffer_percent;
    @BindView(R.id.error_msg)
    TextView error_msg;
    @BindView(R.id.tv_gesture_indicator)
    TextView tv_gesture_indicator;

    private Unbinder mUnBinder;

    private VideoUrl mVideoUrl;

    private String mTitle;

    private CompositeDisposable mDisposables;

    private SystemUiHelper mSystemUiHelper;
    private SystemUiHelper.OnVisibilityChangeListener mOnVisibilityChangeListener;

    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private IMediaPlayer.OnExternalTimedTextListener mOnExternalTimedTextListener;

    private ConnectivityChangeHelper mConnectivityChangeHelper;

    private int mCurrentSubId;

    private Observable<List<String>> mPathObservable;
    private ISubProcessor mSubProcessor;
    private Handler mHandler;
    private ISubProcessorCallback mSubProcessorCallback = new ISubProcessorCallback.Stub() {

        @Override
        public void onProcessStateChanged(int newState) {
            mHandler.sendEmptyMessage(newState);
        }

        @Override
        public void onSubReady(String subFile) {
            Message msg = Message.obtain();
            msg.what = 1000;
            msg.obj = subFile;
            mHandler.sendMessage(msg);
        }
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSubProcessor = ISubProcessor.Stub.asInterface(service);
            int scanState = -1;
            try {
                scanState = mSubProcessor.getProcessState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mHandler.sendEmptyMessage(scanState);
            try {
                mSubProcessor.registerCallback(mSubProcessorCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                mSubProcessor.unregisterCallback(mSubProcessorCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mSubProcessor = null;
        }
    };

    public static VideoPlayerFragment newInstance(VideoUrl videoUrl, String title) {
        VideoPlayerFragment instance = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putSerializable("video_url", videoUrl);
        args.putString("title", title);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            mVideoUrl = (VideoUrl) getArguments().getSerializable("video_url");
            mTitle = getArguments().getString("title");
        }
        mConnectivityChangeHelper = new ConnectivityChangeHelper(getActivity(), new ConnectivityChangeHelper.OnConnectivityChangeListener() {
            @Override
            public void onNetworkUnAvailable() {

            }

            @Override
            public void onWiFiAvailable() {

            }

            @Override
            public void onMobileAvailable() {
                if (!"direct".equals(mVideoUrl.type)) {
                    Alerter.create(getActivity()).setText(R.string.msg_mobile_network).show();
                }
            }
        });

        if ("movie".equals(mVideoUrl.type)
                || "tv_show".equals(mVideoUrl.type)
                || "variety".equals(mVideoUrl.type)
                || "animation".equals(mVideoUrl.type)
                || "video".equals(mVideoUrl.type)
                || "tv".equals(mVideoUrl.type)
                || "web".equals(mVideoUrl.type)) {
            if ("tv".equals(mVideoUrl.type)) {
                mPathObservable = mRestAPI.spunSugarService().tvSources(mVideoUrl.url)
                        .flatMap(videoUrls -> Utils.createParseObservable(getActivity(), videoUrls.get(0), mRestAPI)
                                .onErrorResumeNext(Observable.mergeDelayError(Arrays.asList(Utils.createSnifferObservable(getActivity(), videoUrls.get(0).url, true),
                                        Utils.createSnifferObservable(getActivity(), videoUrls.get(0).url, false))).firstOrError().toObservable()));
            } else if ("web".equals(mVideoUrl.type)) {
                mPathObservable = Observable
                        .mergeDelayError(Arrays.asList(Utils.createSnifferObservable(getActivity(), newUrl("http://v.d9y.net/vip", mVideoUrl.url), true),
                                Utils.createSnifferObservable(getActivity(), newUrl("http://jqaaa.com/jx.php", mVideoUrl.url), true),
                                Utils.createSnifferObservable(getActivity(), newUrl("http://www.82190555.com/video.php", mVideoUrl.url), true),
                                Utils.createSnifferObservable(getActivity(), newUrl("http://jx.du2.cc", mVideoUrl.url), true)))
                        .firstOrError()
                        .toObservable()
                        .onErrorResumeNext(Observable
                                .mergeDelayError(Arrays.asList(Utils.createSnifferObservable(getActivity(), newUrl("http://v.d9y.net/vip", mVideoUrl.url), false),
                                        Utils.createSnifferObservable(getActivity(), newUrl("http://jqaaa.com/jx.php", mVideoUrl.url), false),
                                        Utils.createSnifferObservable(getActivity(), newUrl("http://www.82190555.com/video.php", mVideoUrl.url), false),
                                        Utils.createSnifferObservable(getActivity(), newUrl("http://jx.du2.cc", mVideoUrl.url), false)))
                                .firstOrError()
                                .toObservable());
            } else {
                mPathObservable = Utils.createParseObservable(getActivity(), mVideoUrl, mRestAPI)
                        .onErrorResumeNext(Observable.mergeDelayError(Arrays.asList(Utils.createSnifferObservable(getActivity(), mVideoUrl.url, true),
                                Utils.createSnifferObservable(getActivity(), mVideoUrl.url, false))).firstOrError().toObservable());
            }
            mPathObservable = mPathObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .cache()
                    .onTerminateDetach();
        }
        loadAD();
    }

    private String newUrl(String url, String urlParameter) {
        return HttpUrl.parse(url)
                .newBuilder()
                .addQueryParameter("url", urlParameter)
                .build()
                .toString();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        toolbar.setTitle(mTitle);

        mConnectivityChangeHelper.registerReceiver();

        prepareListeners();
        mSystemUiHelper = new SystemUiHelper(getActivity(), SystemUiHelper.LEVEL_IMMERSIVE, SystemUiHelper.FLAG_IMMERSIVE_STICKY, mOnVisibilityChangeListener);

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) media_controller.getLayoutParams();
        layoutParams.leftMargin = (int) (DimenUtils.getScreenWidth(getActivity()) - DimenUtils.dipToPixels(500, getActivity())) / 2;
        layoutParams.rightMargin = layoutParams.leftMargin;
        media_controller.setLayoutParams(layoutParams);
        media_controller.setUseFastForward(false);
        media_controller.setOnVisibilityChangeListener(visible -> {
            if (visible) {
                mSystemUiHelper.show();
            } else {
                mSystemUiHelper.hide();
            }
        });
        media_controller.setTransportStateListener(new TransportMediator.TransportStateListener() {

            @Override
            public void onPlayingChanged(TransportMediator controller) {
                if (!controller.isPlaying()) {
                    loadAD();
                }
            }

            @Override
            public void onTransportControlsChanged(TransportMediator controller) {

            }
        });

        video_view.setUIPerformer(new GestureMediaController.SimpleGestureTransportController.UIPerformer() {
            @Override
            public void stopAdjustProgress(int requestProgress) {
                video_view.seekTo(video_view.getCurrentPosition() + requestProgress * 1000);
            }

            @Override
            public void showAdjustVolume(int requestVolume, int maxVolume) {
                tv_gesture_indicator.setText(tv_gesture_indicator.getResources().getString(R.string.volume, requestVolume * 100f / maxVolume));
            }

            @Override
            public void showAdjustBrightness(float requestBrightness) {
                tv_gesture_indicator.setText(tv_gesture_indicator.getResources().getString(R.string.brightness, requestBrightness * 100));
            }

            @Override
            public void showAdjustProgress(int requestProgress) {
                tv_gesture_indicator.setText(tv_gesture_indicator.getResources().getString(R.string.progress, requestProgress));
            }

            @Override
            public void startShow() {
                tv_gesture_indicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void stopShow() {
                tv_gesture_indicator.setVisibility(View.GONE);
            }
        });
        video_view.setMediaController(media_controller);
        video_view.setOnInfoListener(mOnInfoListener);
        video_view.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        video_view.setOnExternalTimedTextListener(mOnExternalTimedTextListener);

        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("hardware_acceleration", true)) {
//            video_view.setHardwareDecoder(true);
        }

        video_view.requestFocus();
        media_controller.setEnabled(false);

        btn_subs.setOnClickListener(view1 -> SubtitleFragment
                .newInstance(mTitle.split(" ")[0], mCurrentSubId).show(getFragmentManager(), "SubtitleFragment"));
        btn_scale.setOnClickListener(view12 -> video_view.toggleAspectRatio());
        tv_subtitle.setTextSize(Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("subtitle_font_size", "14")));
        tv_subtitle.setTextColor(PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("subtitle_color", 0xffffff));

        mHandler = new SubProcessorHandler(video_view, mRxBus);
        getActivity().bindService(new Intent(getActivity(), SubProcessorService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

        mDisposables = new CompositeDisposable();
        mDisposables.add(mRxBus.asFlowable()
                .subscribe(event -> {
                    if (event instanceof Sub) {
                        mCurrentSubId = ((Sub) event).id;
                        if (mCurrentSubId != 0) {
                            getActivity().startService(new Intent(getActivity(), SubProcessorService.class).putExtra("sub_id", mCurrentSubId));
                        } else {
                            video_view.addExternalTimedTextSource(null);
                        }
                    }
                }));
        setVideoPath();

        if ("tv".equals(mVideoUrl.type)) {
            btn_scale.setVisibility(View.GONE);
            btn_subs.setVisibility(View.GONE);
        }
        video_view.setKeepScreenOn(true);
        return view;
    }

    @Override
    public void onPause() {
        if (video_view.getCurrentPosition() > 0
                && ("tv_show".equals(mVideoUrl.type)
                || "movie".equals(mVideoUrl.type)
                || "variety".equals(mVideoUrl.type)
                || "animation".equals(mVideoUrl.type)
                || "video".equals(mVideoUrl.type))) {
            mDisposables.add(createWatchHistoryObservable(mVideoUrl)
                    .subscribe(watchHistories -> {
                        WatchHistory watchHistory;
                        if (watchHistories.isEmpty()) {
                            watchHistory = new WatchHistory();
                            watchHistory.subjectType = mVideoUrl.type;
                            watchHistory.subjectId = mVideoUrl.parentId;
                            watchHistory.subjectVideoId = mVideoUrl.id;
                            watchHistory.subjectVideoName = mVideoUrl.name;
                        } else {
                            watchHistory = watchHistories.get(0);
                        }
                        watchHistory.watchTime = (long) video_view.getCurrentPosition();
                        watchHistory.duration = (long) video_view.getDuration();
                        watchHistory.updateTime = new Date();
                        mAppDatabase.watchHistoryDao().insert(watchHistory);
                    }));
        }
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setVideoPath() {
        if (mPathObservable != null) {
            mDisposables.add(mPathObservable
                    .subscribe(
                            uris -> {
                                video_view.setVideoPath(uris.get(0));
                                video_view.start();
                                mDisposables.add(createWatchHistoryObservable(mVideoUrl)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                watchHistories -> {
                                                    if (!watchHistories.isEmpty()) {
                                                        video_view.seekTo(watchHistories.get(0).watchTime.intValue());
                                                    }
                                                },
                                                throwable -> Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show()));
                            },
                            throwable -> {
                                progress_bar.setVisibility(View.GONE);
                                error_msg.setVisibility(View.VISIBLE);
                                error_msg.setText(R.string.error_msg_cannot_play);
                                Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
        } else {
            video_view.setVideoPath(mVideoUrl.url);
            video_view.start();
        }
    }

    private void prepareListeners() {
        mOnVisibilityChangeListener = visible -> {
            if (visible) {
                TranslateAnimation animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, -1,
                        Animation.RELATIVE_TO_SELF, 0);
                animation.setFillAfter(true);
                animation.setDuration(200);
                animation.setInterpolator(new DecelerateInterpolator());
                toolbar.setDrawingCacheEnabled(true);
                toolbar.startAnimation(animation);
            } else {
                TranslateAnimation animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, -1);
                animation.setFillAfter(true);
                animation.setDuration(200);
                animation.setInterpolator(new AccelerateInterpolator());
                toolbar.setDrawingCacheEnabled(true);
                toolbar.startAnimation(animation);
            }
        };
        mOnInfoListener = (mp, what, extra) -> {
            switch (what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    if (video_view.isPlaying()) {
                        progress_bar.setVisibility(View.VISIBLE);
                        download_rate.setText("");
                        buffer_percent.setText("");
                        download_rate.setVisibility(View.VISIBLE);
                        buffer_percent.setVisibility(View.VISIBLE);
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    progress_bar.setVisibility(View.GONE);
                    download_rate.setVisibility(View.GONE);
                    buffer_percent.setVisibility(View.GONE);
                    break;
                case IMediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                    download_rate.setText(getString(R.string.download_rate, extra));
                    break;
            }
            return true;
        };
        mOnBufferingUpdateListener = (mp, percent) -> buffer_percent.setText(getString(R.string.buffer_percent, percent));
        mOnExternalTimedTextListener = caption -> tv_subtitle.setText(caption == null ? "" : Html.fromHtml(caption.content));
    }

    @Override
    public void onDestroyView() {
        getActivity().unbindService(mServiceConnection);
        video_view.setKeepScreenOn(false);
        mDisposables.dispose();
        mUnBinder.unbind();
        mConnectivityChangeHelper.unregisterReceiver();
        super.onDestroyView();
    }

    private Observable<List<WatchHistory>> createWatchHistoryObservable(VideoUrl videoUrl) {
        return Observable.just(videoUrl)
                .flatMap(videoU -> Observable.just(mAppDatabase.watchHistoryDao()
                        .getByIdSync(videoU.parentId, videoU.id)));
    }

    private void loadAD() {
        final InterstitialAD ad = new InterstitialAD(getActivity(), Constants.QQ_ID, Constants.GTD_INTERSTITIAL_ID);
        ad.setADListener(new AbstractInterstitialADListener() {
            @Override
            public void onNoAD(AdError adError) {
                if (video_view != null && !video_view.isPlaying()) {
                    loadAD();
                }
            }

            @Override
            public void onADReceive() {
                if (video_view != null && !video_view.isPlaying()) {
                    ad.show();
                }
            }
        });
        ad.loadAD();
    }

    private static class SubProcessorHandler extends Handler {
        private WeakReference<VideoView> mVideoView;
        private WeakReference<RxBus> mBus;

        SubProcessorHandler(VideoView videoView, RxBus bus) {
            mVideoView = new WeakReference<>(videoView);
            mBus = new WeakReference<>(bus);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1000) {
                VideoView videoView = mVideoView.get();
                if (videoView != null) {
                    videoView.addExternalTimedTextSource(new File(msg.obj.toString()));
                }
            } else {
                RxBus bus = mBus.get();
                if (bus != null) {
                    int subProcessState = msg.what;
                    int subProcessMsgResource = 0;
                    switch (subProcessState) {
                        case SubProcessorService.STATE_DETAIL_START: {
                            subProcessMsgResource = R.string.subtitle_url_loading_msg;
                        }
                        break;
                        case SubProcessorService.STATE_DOWNLOAD_START: {
                            subProcessMsgResource = R.string.subtitle_downloading_msg;
                        }
                        break;
                        case SubProcessorService.STATE_EXTRACT_START: {
                            subProcessMsgResource = R.string.subtitle_extracting_mag;
                        }
                        break;
                        case SubProcessorService.STATE_EXTRACT_SUCCESS: {
                            subProcessMsgResource = R.string.subtitle_success_msg;
                        }
                        break;
                        case SubProcessorService.STATE_ERROR: {
                            subProcessMsgResource = R.string.subtitle_error_msg;
                        }
                        break;
                        default:
                            break;
                    }
                    bus.send(subProcessMsgResource);
                }
            }
        }
    }
}

package com.lcjian.cloudlocation.ui.device;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.network.entity.MonitorInfo;
import com.lcjian.cloudlocation.data.network.entity.Route;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HistoryPathActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;
    @BindView(R.id.cl_history_path)
    ConstraintLayout cl_history_path;
    @BindView(R.id.v_map)
    MapView v_map;
    @BindView(R.id.cv_change_map_layer_history_path)
    ImageView cv_change_map_layer_history_path;
    @BindView(R.id.iv_zoom_in)
    ImageView iv_zoom_in;
    @BindView(R.id.iv_zoom_out)
    ImageView iv_zoom_out;

    @BindView(R.id.btn_pre_date)
    ImageButton btn_pre_date;
    @BindView(R.id.tv_date)
    TextView tv_date;
    @BindView(R.id.btn_next_date)
    ImageButton btn_next_date;

    @BindView(R.id.btn_play)
    ImageButton btn_play;
    @BindView(R.id.sb_progress)
    SeekBar sb_progress;
    @BindView(R.id.sb_speed)
    SeekBar sb_speed;

    private BaiduMap mBMap;
    private Overlay mStartOverlay;
    private Overlay mEndOverlay;
    private Overlay mSportOverlay;
    private Overlay mRouteOverlay;
    private List<Overlay> mPositionOverlays;

    private int mMapType = BaiduMap.MAP_TYPE_NORMAL;

    private MonitorInfo.MonitorDevice mMonitorDevice;

    private Date mDate;

    private boolean mLbs;
    private boolean mFollow = true;
    private boolean mLine = true;
    private boolean mDot;

    private List<Route.Position> mPositions;
    private List<Route.Position> mProgressPositions;

    private int mProgress;

    private int mPlaySpeed = 5;

    private Disposable mDisposable;
    private Disposable mDisposableS;
    private Disposable mDisposableCountDown;

    private boolean mSeek;
    private boolean mDragging;

    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            mProgress = progress;
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            seek();
        }
    };

    private SeekBar.OnSeekBarChangeListener mSpeedSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            mPlaySpeed = progress;
            if (mPlaySpeed == 0) {
                mPlaySpeed = 1;
            }
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            changeSpeed();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_path);
        ButterKnife.bind(this);
        mMonitorDevice = (MonitorInfo.MonitorDevice) getIntent().getSerializableExtra("monitor_device");
        mDate = new Date();

        tv_title.setText(R.string.history_path);
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.lsgj_sz);

        btn_nav_back.setOnClickListener(this);
        btn_nav_right.setOnClickListener(this);
        cv_change_map_layer_history_path.setOnClickListener(this);
        iv_zoom_in.setOnClickListener(this);
        iv_zoom_out.setOnClickListener(this);
        tv_date.setOnClickListener(this);
        btn_pre_date.setOnClickListener(this);
        btn_next_date.setOnClickListener(this);
        btn_play.setOnClickListener(this);

        sb_progress.setOnSeekBarChangeListener(mSeekListener);
        sb_speed.setOnSeekBarChangeListener(mSpeedSeekListener);

        setupPlayControl();

        // 地图设置
        v_map.showZoomControls(false);
        mBMap = v_map.getMap();
        mBMap.getUiSettings().setCompassEnabled(false);
        mBMap.setMapType(mMapType);

        mDisposableS = mRxBus.asFlowable()
                .filter(o -> o instanceof HistoryPathSettingFragment.HistoryPathSettingEvent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    HistoryPathSettingFragment.HistoryPathSettingEvent event = (HistoryPathSettingFragment.HistoryPathSettingEvent) o;
                    boolean changed = false;
                    if (mLbs != event.lbs) {
                        mLbs = event.lbs;
                        changed = true;
                    }
                    if (mFollow != event.follow) {
                        mFollow = event.follow;
                    }
                    if (mLine != event.line) {
                        mLine = event.line;
                    }
                    if (mDot != event.dot) {
                        mDot = event.dot;
                    }
                    if (mPositions != null && !mPositions.isEmpty()) {
                        drawStartEnd();
                        drawPositions();
                    }
                    if (changed) {
                        getData();
                    }
                });
        getData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        v_map.onResume();
    }

    @Override
    protected void onPause() {
        v_map.onPause();
        pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mDisposableS != null) {
            mDisposableS.dispose();
        }
        if (mDisposableCountDown != null) {
            mDisposableCountDown.dispose();
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        v_map.onDestroy();
        super.onDestroy();
    }

    private void getData() {
        pause();
        mBMap.clear();
        mStartOverlay = null;
        mEndOverlay = null;
        mSportOverlay = null;
        mRouteOverlay = null;
        mPositionOverlays = null;
        mPositions = null;
        mProgressPositions = null;

        tv_date.setText(DateUtils.convertDateToStr(mDate, "yyyy/MM/dd"));
        showProgress();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = mRestAPI.cloudService().getDeviceRoute(
                Long.parseLong(mMonitorDevice.id),
                DateUtils.convertDateToStr(mDate) + " 00:00",
                DateUtils.convertDateToStr(mDate) + " 23:59",
                mLbs ? 1 : 0,
                1000,
                mUserInfoSp.getString("sign_in_map", "Google"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(route -> {
                            hideProgress();
                            if (route.list != null) {
                                mPositions = route.list;

                                if (!mPositions.isEmpty()) {
                                    mBMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                                            .target(new LatLng(Double.parseDouble(mPositions.get(0).lat), Double.parseDouble(mPositions.get(0).lng)))
                                            .zoom(16)
                                            .build()));
                                    drawStartEnd();
                                    drawPositions();
                                }
                            }
                            setupPlayControl();
                        },
                        throwable -> hideProgress());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nav_back:
                onBackPressed();
                break;
            case R.id.btn_play:
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
                setupPlayControl();
                break;
            case R.id.btn_nav_right:
                HistoryPathSettingFragment.newInstance(mLbs, mFollow, mLine, mDot).show(getSupportFragmentManager(), "HistoryPathSettingFragment");
                break;
            case R.id.tv_date:
                startActivityForResult(new Intent(v.getContext(), DatePickerActivity.class), 1000);
                break;
            case R.id.btn_pre_date:
                mDate = DateUtils.addDays(mDate, -1);
                getData();
                break;
            case R.id.btn_next_date:
                Date date = DateUtils.addDays(mDate, 1);
                if (DateUtils.isAfter(date, new Date())) {
                    return;
                } else {
                    mDate = date;
                }
                getData();
                break;
            case R.id.iv_zoom_in:
                mBMap.animateMapStatus(MapStatusUpdateFactory.zoomIn());
                break;
            case R.id.iv_zoom_out:
                mBMap.animateMapStatus(MapStatusUpdateFactory.zoomOut());
                break;
            case R.id.cv_change_map_layer_history_path:
                if (mMapType == BaiduMap.MAP_TYPE_NORMAL) {
                    mMapType = BaiduMap.MAP_TYPE_SATELLITE;
                } else {
                    mMapType = BaiduMap.MAP_TYPE_NORMAL;
                }
                mBMap.setMapType(mMapType);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                mDate = new Date(data.getLongExtra("date", 0));
                getData();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void drawStartEnd() {
        Route.Position start = mPositions.get(0);
        Route.Position end = mPositions.get(mPositions.size() - 1);

        if (mStartOverlay != null) {
            mStartOverlay.remove();
        }
        OverlayOptions makerOptionStart = new MarkerOptions()
                .position(new LatLng(Double.parseDouble(start.lat), Double.parseDouble(start.lng)))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.hdw));
        mStartOverlay = mBMap.addOverlay(makerOptionStart);

        if (mEndOverlay != null) {
            mEndOverlay.remove();
        }
        OverlayOptions makerOptionEnd = new MarkerOptions()
                .position(new LatLng(Double.parseDouble(end.lat), Double.parseDouble(end.lng)))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.hdw1));
        mEndOverlay = mBMap.addOverlay(makerOptionEnd);
    }

    private void drawSport() {
        Route.Position sport;
        if (mProgressPositions == null || mProgressPositions.isEmpty()) {
            sport = mPositions.get(0);
        } else {
            sport = mProgressPositions.get(mProgressPositions.size() - 1);
        }

        if (mSportOverlay != null) {
            mSportOverlay.remove();
        }
        View makerView = LayoutInflater.from(this).inflate(R.layout.device_maker_sport_item, cl_history_path, false);
        TextView tv_device_info_detail = makerView.findViewById(R.id.tv_device_info_detail);
        ImageView iv_device_status = makerView.findViewById(R.id.iv_device_status);

        iv_device_status.setRotation(Float.parseFloat(sport.c));

        String strStatus;
        if ("1".equals(sport.stop)) {
            strStatus = getString(R.string.status_static);
        } else {
            strStatus = getString(R.string.moving);
        }

        double course = Double.parseDouble(sport.c);
        String courseStatus = "";
        if (((course >= 0 && course < 22.5) || (course >= 337.5 && course < 360) || course >= 360)) {
            courseStatus = getString(R.string.direction_north);
        } else if (course >= 22.5 && course < 67.5) {
            courseStatus = getString(R.string.direction_northeast);
        } else if (course >= 67.5 && course < 112.5) {
            courseStatus = getString(R.string.direction_east);
        } else if (course >= 112.5 && course < 157.5) {
            courseStatus = getString(R.string.direction_southeast);
        } else if (course >= 157.5 && course < 202.5) {
            courseStatus = getString(R.string.direction_south);
        } else if (course >= 202.5 && course < 247.5) {
            courseStatus = getString(R.string.direction_southwest);
        } else if (course >= 247.5 && course < 292.5) {
            courseStatus = getString(R.string.direction_west);
        } else if (course >= 292.5 && 337.5 > course) {
            courseStatus = getString(R.string.direction_northwest);
        }

        String detail = mMonitorDevice.name + "\n"
                + (TextUtils.equals("1", sport.g) ? "GPS"
                : TextUtils.equals("2", sport.g) ? "LBS"
                : TextUtils.equals("3", sport.g) ? "WIFI" : "") + "\n"
                + strStatus + "\n"
                + getString(R.string.device_time) + sport.pt + "\n"
                + getString(R.string.device_speed) + sport.s + "\n"
                + getString(R.string.device_direction) + courseStatus + "\n";
        tv_device_info_detail.setText(detail);

        LatLng latLng = new LatLng(Double.parseDouble(sport.lat), Double.parseDouble(sport.lng));
        OverlayOptions makerOption = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromView(makerView))
                .anchor(0.5f, 0.92857f);
        mSportOverlay = mBMap.addOverlay(makerOption);

        if (mFollow) {
            mBMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                    .target(latLng)
                    .build()));
        }
    }

    private void drawRoute() {
        if (mRouteOverlay != null) {
            mRouteOverlay.remove();
        }
        if (mLine) {
            List<LatLng> latLngs = new ArrayList<>();
            for (Route.Position position : mProgressPositions) {
                latLngs.add(new LatLng(Double.parseDouble(position.lat), Double.parseDouble(position.lng)));
            }
            if (latLngs.size() < 2) {
                return;
            }
            OverlayOptions mRouteOptions = new PolylineOptions().width(13)
                    .color(0xff087ff7)
                    .points(latLngs);
            mRouteOverlay = mBMap.addOverlay(mRouteOptions);
        }
    }

    private void drawPositions() {
        if (mPositionOverlays != null) {
            for (Overlay o : mPositionOverlays) {
                o.remove();
            }
        }
        if (mDot) {
            List<OverlayOptions> options = new ArrayList<>();
            for (Route.Position p : mPositions) {
                OverlayOptions makerOption = new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(p.lat), Double.parseDouble(p.lng)))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.yuanquan))
                        .anchor(0.5f, 0.5f);
                options.add(makerOption);
            }
            mPositionOverlays = mBMap.addOverlays(options);
        }
    }

    private void play() {
        if (mPositions == null || mPositions.isEmpty()) {
            return;
        }
        if (!isPlaying()) {
            if (mSeek) {
                mProgressPositions = mPositions.subList(0, mProgress * mPositions.size() / 1000);
                mSeek = false;
            }
            mDisposableCountDown = io.reactivex.Observable.interval(5000 / mPlaySpeed, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        if (mProgressPositions == null ||
                                mProgressPositions.size() + 1 <= mPositions.size()) {
                            mProgressPositions = mPositions.subList(0, (mProgressPositions == null ? 0 : mProgressPositions.size()) + 1);
                            drawRoute();
                            drawSport();
                        } else {
                            pause();
                        }
                        setupPlayControl();
                    });
        }
    }

    private void pause() {
        if (isPlaying()) {
            mDisposableCountDown.dispose();
        }
    }

    private boolean isPlaying() {
        return mDisposableCountDown != null && !mDisposableCountDown.isDisposed();
    }

    private void setupPlayControl() {
        if (mDragging) {
            return;
        }
        if (mPositions == null || mPositions.isEmpty()) {
            btn_play.setEnabled(false);
            sb_progress.setEnabled(false);
            sb_speed.setEnabled(false);
        } else {
            btn_play.setEnabled(true);
            sb_progress.setEnabled(true);
            sb_speed.setEnabled(true);
        }
        btn_play.setImageResource(isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        sb_progress.setProgress(mProgressPositions == null ? 0 : (mProgressPositions.size() * 1000 / mPositions.size()));
        sb_speed.setProgress(mPlaySpeed);
    }

    private void seek() {
        mSeek = true;
        pause();
        play();
    }

    private void changeSpeed() {
        pause();
        play();
    }
}

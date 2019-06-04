package com.lcjian.cloudlocation.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.DistanceUtil;
import com.google.gson.Gson;
import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.network.entity.MonitorInfo;
import com.lcjian.cloudlocation.ui.base.BaseFragment;
import com.lcjian.cloudlocation.ui.device.DeviceInfoActivity;
import com.lcjian.cloudlocation.ui.device.DevicesActivity;
import com.lcjian.cloudlocation.ui.device.GEOFenceListActivity;
import com.lcjian.cloudlocation.ui.device.HistoryPathActivity;
import com.lcjian.cloudlocation.ui.device.PanoramaActivity;
import com.lcjian.cloudlocation.ui.device.RealTimeMonitorActivity;
import com.lcjian.cloudlocation.ui.web.MessageSettingActivity;
import com.lcjian.cloudlocation.ui.web.SendCommandActivity;
import com.lcjian.cloudlocation.util.DateUtils;
import com.lcjian.cloudlocation.util.DimenUtils;
import com.lcjian.cloudlocation.util.MapUtils;
import com.lcjian.cloudlocation.util.Spans;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class HomeContentFragment extends BaseFragment implements SensorEventListener, View.OnClickListener {

    ViewGroup mView;
    @BindView(R.id.v_map)
    MapView v_map;
    @BindView(R.id.tv_click_to_refresh)
    TextView tv_click_to_refresh;
    @BindView(R.id.tv_countdown)
    TextView tv_countdown;
    @BindView(R.id.fl_countdown)
    FrameLayout fl_countdown;
    @BindView(R.id.cv_change_map_layer)
    ImageView cv_change_map_layer;
    @BindView(R.id.cv_change_to_panorama)
    ImageView cv_change_to_panorama;
    @BindView(R.id.iv_show_distance)
    ImageView iv_show_distance;
    @BindView(R.id.iv_go_to_device_location)
    ImageView iv_go_to_device_location;
    @BindView(R.id.iv_go_to_my_location)
    ImageView iv_go_to_my_location;
    @BindView(R.id.cv_go_to_map_navigation)
    CardView cv_go_to_map_navigation;
    @BindView(R.id.iv_zoom_in)
    ImageView iv_zoom_in;
    @BindView(R.id.iv_zoom_out)
    ImageView iv_zoom_out;
    @BindView(R.id.tv_distance)
    TextView tv_distance;
    @BindView(R.id.btn_search_device)
    TextView btn_search_device;

    Unbinder unbinder;

    private BaiduMap mBMap;
    private LocationClient mLocClient;
    private SensorManager mSensorManager;

    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccuracy;
    private MyLocationData mLocData;

    private List<Overlay> mDeviceMakers;
    private Polyline mPolyline;

    private int mMapType = BaiduMap.MAP_TYPE_NORMAL;
    private boolean mShowDistance = true;
    private boolean mChoose;
    private boolean mShowWindow = true;

    private PublishSubject<Long> subjectCountDown;
    private PublishSubject<LocationReceivedEvent> subjectLocation;
    private PublishSubject<CurrentDeviceChangeEvent> subjectDeviceChange;

    private Disposable mDisposableRefresh;
    private Disposable mDisposableCountdown;
    private Disposable mDisposableGeoFence;
    private Disposable mDisposableShowDistance;
    private Disposable mDisposableMakers;

    private MonitorInfo.MonitorDevice mCurrentDevice;
    private MonitorInfo.MonitorDevice origin;
    private List<MonitorInfo.MonitorDevice> monitorDevices;

    public static HomeContentFragment newInstance(MonitorInfo.MonitorDevice origin) {
        HomeContentFragment fragment = new HomeContentFragment();
        Bundle args = new Bundle();
        args.putSerializable("origin", origin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            origin = (MonitorInfo.MonitorDevice) getArguments().getSerializable("origin");
        }
        mDeviceMakers = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = (ViewGroup) inflater.inflate(R.layout.fragment_home_content, container, false);
        unbinder = ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        subjectCountDown = PublishSubject.create();
        subjectLocation = PublishSubject.create();
        subjectDeviceChange = PublishSubject.create();
        mSensorManager = (SensorManager) view.getContext().getSystemService(Context.SENSOR_SERVICE);

        fl_countdown.setOnClickListener(this);
        cv_change_to_panorama.setOnClickListener(this);
        cv_change_map_layer.setOnClickListener(this);
        cv_go_to_map_navigation.setOnClickListener(this);
        iv_zoom_in.setOnClickListener(this);
        iv_zoom_out.setOnClickListener(this);
        iv_go_to_my_location.setOnClickListener(this);
        iv_go_to_device_location.setOnClickListener(this);
        iv_show_distance.setOnClickListener(this);
        btn_search_device.setOnClickListener(this);
        btn_search_device.setText(new Spans()
                .append("*", new ImageSpan(view.getContext(), R.drawable.sy_ssan))
                .append(" ")
                .append(getString(R.string.search_device)));

        if (origin != null || getSignInInfo().userInfo == null) {
            btn_search_device.setVisibility(View.GONE);
        }

        // 地图设置
        v_map.showZoomControls(false);
        mBMap = v_map.getMap();
        mBMap.getUiSettings().setCompassEnabled(false);
        mBMap.setMapType(mMapType);
        mBMap.setMyLocationEnabled(true);
        mBMap.setMyLocationConfiguration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));
        mBMap.setOnMarkerClickListener(marker -> {
            MonitorInfo.MonitorDevice device = (MonitorInfo.MonitorDevice) marker.getExtraInfo().getSerializable("monitor_device");
            assert device != null;
            if (mCurrentDevice != null && TextUtils.equals(device.id, mCurrentDevice.id)) {
                mShowWindow = !mShowWindow;
            } else {
                mShowWindow = true;
            }

            marker.setToTop();
            mCurrentDevice = device;
            mUserInfoSp.edit().putString("current_device", new Gson().toJson(mCurrentDevice)).apply();
            mBMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                    .target(new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)))
                    .build()));
            subjectDeviceChange.onNext(new CurrentDeviceChangeEvent());
            setupInfoWindowVisibility();
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        v_map.onResume();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        mLocClient = new LocationClient(getContext());
        mLocClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (location == null || v_map == null) {
                    return;
                }
                mCurrentLat = location.getLatitude();
                mCurrentLon = location.getLongitude();
                mCurrentAccuracy = location.getRadius();

                mLocData = new MyLocationData.Builder()
                        .latitude(mCurrentLat)
                        .longitude(mCurrentLon)
                        .accuracy(mCurrentAccuracy)
                        .direction(mCurrentDirection)
                        .build();
                mBMap.setMyLocationData(mLocData);

                subjectLocation.onNext(new LocationReceivedEvent());
            }
        });
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(10 * 1000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        mDisposableGeoFence = mRxBus.asFlowable()
                .filter(o -> o instanceof MainActivity.GEOFenceEvent || o instanceof MainActivity.MessageSettingEvent)
                .subscribe(o -> {
                    if (o instanceof MainActivity.GEOFenceEvent) {
                        mView.getContext().startActivity(new Intent(mView.getContext(), GEOFenceListActivity.class)
                                .putExtra("monitor_device", mCurrentDevice));
                    } else {
                        mView.getContext().startActivity(new Intent(mView.getContext(), MessageSettingActivity.class)
                                .putExtra("device_id", mCurrentDevice.id));
                    }
                });
        mDisposableRefresh = Observable.combineLatest(
                subjectCountDown,
                subjectDeviceChange,
                (aLong, currentDeviceChangeEvent) -> new Object())
                .observeOn(Schedulers.io())
                .flatMap(aLong -> Single.zip(
                        mRestAPI.cloudService().getTrack(Long.parseLong(mCurrentDevice.id), mCurrentDevice.model, "", "Baidu"),
                        mRestAPI.cloudService().getAddressByLatLng(mCurrentDevice.lat, mCurrentDevice.lng, "Baidu"),
                        (device, address) -> {
                            device.address = address.address;
                            return device;
                        })
                        .toObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(monitorDevice -> {
                    monitorDevice.id = mCurrentDevice.id;
                    monitorDevice.name = mCurrentDevice.name;
                    monitorDevice.model = mCurrentDevice.model;

                    mCurrentDevice = monitorDevice;
                    Overlay currentDeviceMaker = null;
                    for (Overlay marker : mDeviceMakers) {
                        MonitorInfo.MonitorDevice device = (MonitorInfo.MonitorDevice) marker.getExtraInfo().getSerializable("monitor_device");
                        if (TextUtils.equals(device.id, mCurrentDevice.id)) {
                            currentDeviceMaker = marker;
                            break;
                        }
                    }
                    if (currentDeviceMaker != null) {
                        mDeviceMakers.remove(currentDeviceMaker);
                        currentDeviceMaker.remove();
                        mDeviceMakers.add(mapAddDeviceMarker(mCurrentDevice));
                    }
                    if (mChoose) {
                        mBMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                                .target(new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)))
                                .zoom(18)
                                .build()));
                        mChoose = false;
                    }
                    setupDistanceVisibility();
                    setupInfoWindowVisibility();
                    countdown();
                }, throwable -> countdown());

        mDisposableShowDistance = Observable.combineLatest(
                subjectLocation.firstOrError().toObservable(),
                subjectDeviceChange,
                (locationReceivedEvent, currentDeviceChangeEvent) -> new Object())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> setupDistanceVisibility());

        if (monitorDevices == null) {
            mDisposableMakers = Observable.just(getSignInInfo())
                    .flatMap(signInInfo -> {
                        if (origin != null || getSignInInfo().deviceInfo != null) {
                            return mRestAPI.cloudService()
                                    .getTrack(Long.parseLong(origin == null ? getSignInInfo().deviceInfo.deviceID : origin.id),
                                            origin == null ? getSignInInfo().deviceInfo.model : origin.model,
                                            "",
                                            "Baidu")
                                    .map(monitorDevice -> {
                                        MonitorInfo monitorInfo = new MonitorInfo();
                                        if (origin == null) {
                                            monitorDevice.id = getSignInInfo().deviceInfo.deviceID;
                                            monitorDevice.name = getSignInInfo().deviceInfo.deviceName;
                                            monitorDevice.model = getSignInInfo().deviceInfo.model;
                                        } else {
                                            monitorDevice.id = origin.id;
                                            monitorDevice.name = origin.name;
                                            monitorDevice.model = origin.model;
                                        }
                                        monitorInfo.devices = new ArrayList<>();
                                        monitorInfo.devices.add(monitorDevice);
                                        return monitorInfo;
                                    })
                                    .toObservable();
                        } else {
                            return mRestAPI.cloudService().monitorDevices(
                                    Long.parseLong(getSignInInfo().userInfo.userID), "", "Baidu").toObservable();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(monitorInfo -> {
                        for (Overlay overlay : mDeviceMakers) {
                            overlay.remove();
                        }
                        monitorDevices = monitorInfo.devices;

                        for (MonitorInfo.MonitorDevice device : monitorInfo.devices) {
                            mDeviceMakers.add(mapAddDeviceMarker(device));
                        }

                        if (mCurrentDevice == null && monitorInfo.devices != null && !monitorInfo.devices.isEmpty()) {
                            mCurrentDevice = getLastDevice(monitorInfo.devices);
                            if (mCurrentDevice == null) {
                                mCurrentDevice = monitorInfo.devices.get(0);
                            }
                            mBMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                                    .target(new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)))
                                    .zoom(18)
                                    .build()));
                            subjectDeviceChange.onNext(new CurrentDeviceChangeEvent());
                            setupDistanceVisibility();
                            startRefresh();
                        }
                    }, throwable -> {
                    });
        } else {
            v_map.post(() -> {
                if (mCurrentDevice == null && !monitorDevices.isEmpty()) {
                    mCurrentDevice = getLastDevice(monitorDevices);
                    if (mCurrentDevice == null) {
                        mCurrentDevice = monitorDevices.get(0);
                    }
                }

                subjectDeviceChange.onNext(new CurrentDeviceChangeEvent());
                setupDistanceVisibility();
                startRefresh();
            });
        }
    }

    @Override
    public void onPause() {
        if (mDisposableMakers != null) {
            mDisposableMakers.dispose();
        }
        if (mDisposableShowDistance != null) {
            mDisposableShowDistance.dispose();
        }
        if (mDisposableGeoFence != null) {
            mDisposableGeoFence.dispose();
        }
        if (mDisposableCountdown != null) {
            mDisposableCountdown.dispose();
        }
        if (mDisposableRefresh != null) {
            mDisposableRefresh.dispose();
        }
        mLocClient.stop();
        v_map.onPause();
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mBMap.setMyLocationEnabled(false);
        v_map.onDestroy();
        unbinder.unbind();
        mView = null;
        super.onDestroyView();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            mLocData = new MyLocationData.Builder()
                    .latitude(mCurrentLat)
                    .longitude(mCurrentLon)
                    .accuracy(mCurrentAccuracy)
                    .direction(mCurrentDirection)
                    .build();
            mBMap.setMyLocationData(mLocData);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private MonitorInfo.MonitorDevice getLastDevice(List<MonitorInfo.MonitorDevice> devices) {
        MonitorInfo.MonitorDevice device = new Gson()
                .fromJson(mUserInfoSp.getString("current_device", ""),
                        MonitorInfo.MonitorDevice.class);
        if (device != null) {
            for (MonitorInfo.MonitorDevice d : devices) {
                if (TextUtils.equals(device.id, d.id)) {
                    return d;
                }
            }
        }
        return null;
    }

    private void countdown() {
        if (mDisposableCountdown != null) {
            mDisposableCountdown.dispose();
        }
        fl_countdown.setEnabled(true);
        tv_click_to_refresh.setText(R.string.click_to_refresh);
        mDisposableCountdown = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    long c = 15 - aLong;
                    tv_countdown.setText(String.valueOf(c));
                    if (c == 0) {
                        startRefresh();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_countdown:
                startRefresh();
                break;
            case R.id.iv_zoom_in:
                mBMap.animateMapStatus(MapStatusUpdateFactory.zoomIn());
                break;
            case R.id.iv_zoom_out:
                mBMap.animateMapStatus(MapStatusUpdateFactory.zoomOut());
                break;
            case R.id.iv_go_to_my_location:
                mBMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                        .target(new LatLng(mCurrentLat, mCurrentLon))
                        .build()));
                break;
            case R.id.iv_go_to_device_location:
                if (mCurrentDevice != null) {
                    mBMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                            .target(new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)))
                            .build()));
                }
                break;
            case R.id.iv_show_distance:
                mShowDistance = !mShowDistance;
                setupDistanceVisibility();
                if (mShowDistance) {
                    LatLng start = new LatLng(mCurrentLat, mCurrentLon);
                    LatLng end = new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng));
                    mBMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(new LatLngBounds.Builder()
                            .include(start)
                            .include(end)
                            .build()));
                }
                break;
            case R.id.cv_change_map_layer:
                if (mMapType == BaiduMap.MAP_TYPE_NORMAL) {
                    mMapType = BaiduMap.MAP_TYPE_SATELLITE;
                } else {
                    mMapType = BaiduMap.MAP_TYPE_NORMAL;
                }
                mBMap.setMapType(mMapType);
                break;
            case R.id.cv_change_to_panorama:
                if (mCurrentDevice != null) {
                    startActivity(new Intent(v.getContext(), PanoramaActivity.class)
                            .putExtra("longitude", Double.parseDouble(mCurrentDevice.lng))
                            .putExtra("latitude", Double.parseDouble(mCurrentDevice.lat)));
                }
                break;
            case R.id.cv_go_to_map_navigation:
                if (mCurrentDevice != null) {
                    if (MapUtils.haveBaiduMap(v.getContext())) {
                        MapUtils.openBaiduMap(v.getContext(),
                                new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)),
                                mCurrentDevice.name);
                    } else if (MapUtils.haveGaodeMap(v.getContext())) {
                        MapUtils.openGaodeMap(v.getContext(),
                                new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)),
                                mCurrentDevice.name);
                    } else if (MapUtils.haveTencentMap(v.getContext())) {
                        MapUtils.openTentcentMap(v.getContext(),
                                new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)),
                                mCurrentDevice.name);
                    } else {
                        Toast.makeText(App.getInstance(), R.string.no_map_app, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.btn_search_device:
                startActivityForResult(new Intent(v.getContext(), DevicesActivity.class), 1000);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                for (MonitorInfo.MonitorDevice d : monitorDevices) {
                    if (TextUtils.equals(d.id, data.getStringExtra("device_id"))) {
                        mCurrentDevice = d;
                        mUserInfoSp.edit().putString("current_device", new Gson().toJson(mCurrentDevice)).apply();
                        mChoose = true;
                        break;
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Overlay mapAddDeviceMarker(MonitorInfo.MonitorDevice device) {
        View makerView = LayoutInflater.from(mView.getContext()).inflate(R.layout.device_maker_item, mView, false);
        ImageView iv_device_status = makerView.findViewById(R.id.iv_device_status);
        FrameLayout fl_device_name = makerView.findViewById(R.id.fl_device_name);
        TextView tv_device_name = makerView.findViewById(R.id.tv_device_name);

        if (TextUtils.equals("3", device.status.split("-")[0])) {
            iv_device_status.setImageResource(R.drawable.hui_s);
            fl_device_name.setBackgroundResource(R.drawable.pop_offline);
            tv_device_name.setTextColor(0xffbbbbbb);
        } else {
            iv_device_status.setImageResource(R.drawable.lv_s);
            fl_device_name.setBackgroundResource(R.drawable.pop_static);
            tv_device_name.setTextColor(0xff0cc315);
        }
        iv_device_status.setRotation(Float.parseFloat(device.course));
        tv_device_name.setText(device.name);

        Bundle extraInfo = new Bundle();
        extraInfo.putSerializable("monitor_device", device);
        OverlayOptions makerOption = new MarkerOptions()
                .position(new LatLng(Double.parseDouble(device.lat), Double.parseDouble(device.lng)))
                .icon(BitmapDescriptorFactory.fromView(makerView))
                .extraInfo(extraInfo)
                .anchor(0.1f, 0.19f);
        return mBMap.addOverlay(makerOption);
    }

    private void setupInfoWindowVisibility() {
        if (!mShowWindow) {
            mBMap.hideInfoWindow();
            return;
        }
        View markView = LayoutInflater.from(mView.getContext()).inflate(R.layout.window_device_info, mView, false);
        ConstraintLayout cl_device_info = markView.findViewById(R.id.cl_device_info);
        TextView tv_device_name = markView.findViewById(R.id.tv_device_name);
        TextView tv_device_info_detail = markView.findViewById(R.id.tv_device_info_detail);
        View v_divider_2 = markView.findViewById(R.id.v_divider_2);
        TextView tv_go_to_history_path = markView.findViewById(R.id.tv_go_to_history_path);
        TextView tv_go_to_monitor = markView.findViewById(R.id.tv_go_to_monitor);
        TextView tv_go_to_command = markView.findViewById(R.id.tv_go_to_command);
        TextView tv_go_to_device_info = markView.findViewById(R.id.tv_go_to_device_info);
        if (origin != null) {
            markView.setBackgroundResource(R.drawable.fdx);
            ViewGroup.LayoutParams paramsV = markView.getLayoutParams();
            paramsV.height = DimenUtils.spToPixels(216, mView.getContext());
            markView.setLayoutParams(paramsV);

            ViewGroup.LayoutParams paramsC = cl_device_info.getLayoutParams();
            paramsC.height = DimenUtils.spToPixels(210, mView.getContext());
            cl_device_info.setLayoutParams(paramsC);

            v_divider_2.setVisibility(View.GONE);
            tv_go_to_history_path.setVisibility(View.GONE);
            tv_go_to_monitor.setVisibility(View.GONE);
            tv_go_to_command.setVisibility(View.GONE);
            tv_go_to_device_info.setVisibility(View.GONE);
        }

        String strStatus;
        switch (mCurrentDevice.status.split("-")[0]) {
            case "0":
                strStatus = "未启用";
                break;
            case "1":
                strStatus = "运动";
                break;
            case "2":
                strStatus = "静止";
                break;
            case "3":
                strStatus = "离线";
                break;
            case "4":
                strStatus = "欠费";
                break;
            default:
                strStatus = "未启用";
                break;
        }
        double course = Double.parseDouble(mCurrentDevice.course);
        String courseStatus = "";
        if (((course >= 0 && course < 22.5) || (course >= 337.5 && course < 360) || course >= 360)) {
            courseStatus = "北";
        } else if (course >= 22.5 && course < 67.5) {
            courseStatus = "东北";
        } else if (course >= 67.5 && course < 112.5) {
            courseStatus = "东";
        } else if (course >= 112.5 && course < 157.5) {
            courseStatus = "东南";
        } else if (course >= 157.5 && course < 202.5) {
            courseStatus = "南";
        } else if (course >= 202.5 && course < 247.5) {
            courseStatus = "西南";
        } else if (course >= 247.5 && course < 292.5) {
            courseStatus = "西";
        } else if (course >= 292.5 && 337.5 > course) {
            courseStatus = "西北";
        }

        String detail = "状态：" + strStatus + "(" + DateUtils.getPeriod(DateUtils.convertStrToDate(mCurrentDevice.positionTime, DateUtils.YYYY_MM_DD_HH_MM_SS), DateUtils.now()) + ")" + "\n"
                + (TextUtils.equals("1", mCurrentDevice.isGPS) ? "gps" : "wifi") + "\n"
                + "时间:" + mCurrentDevice.positionTime + "\n"
                + "方向:" + courseStatus + "\n"
                + "地址:" + (TextUtils.isEmpty(mCurrentDevice.address) ? "" : mCurrentDevice.address) + "\n";
        tv_device_name.setText(mCurrentDevice.name);
        tv_device_info_detail.setText(detail);

        tv_go_to_history_path.setOnClickListener(v -> v.getContext().startActivity(
                new Intent(v.getContext(), HistoryPathActivity.class)
                        .putExtra("monitor_device", mCurrentDevice)));
        tv_go_to_monitor.setOnClickListener(v -> v.getContext().startActivity(
                new Intent(v.getContext(), RealTimeMonitorActivity.class)
                        .putExtra("monitor_device", mCurrentDevice)));
        tv_go_to_command.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), SendCommandActivity.class)
                .putExtra("device_id", mCurrentDevice.id)));
        tv_go_to_device_info.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), DeviceInfoActivity.class)
                .putExtra("device_id", mCurrentDevice.id)));

        InfoWindow mInfoWindow = new InfoWindow(markView,
                new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)),
                -10);
        mBMap.showInfoWindow(mInfoWindow);
    }

    private void setupDistanceVisibility() {
        if (mCurrentLat == 0 || mCurrentDevice == null) {
            return;
        }
        if (mShowDistance) {
            if (mPolyline != null) {
                mPolyline.remove();
                mPolyline = null;
            }
            LatLng start = new LatLng(mCurrentLat, mCurrentLon);
            LatLng end = new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng));
            mPolyline = (Polyline) mBMap.addOverlay(new PolylineOptions()
                    .width(10)
                    .color(Color.GREEN)
                    .points(Arrays.asList(start, end)));

            double distance = DistanceUtil.getDistance(start, end);
            String s = "相距" + (distance > 1000 ? new DecimalFormat("0.00").format(distance / 1000) + "Km" : distance + "m");
            tv_distance.setText(s);
            tv_distance.setVisibility(View.VISIBLE);
        } else {
            if (mPolyline != null) {
                mPolyline.remove();
                mPolyline = null;
            }
            tv_distance.setVisibility(View.GONE);
        }
    }

    private void startRefresh() {
        fl_countdown.setEnabled(false);
        if (mDisposableCountdown != null) {
            mDisposableCountdown.dispose();
        }
        tv_click_to_refresh.setText(getString(R.string.refreshing));
        subjectCountDown.onNext(0L);
    }

    private class CurrentDeviceChangeEvent {

    }

    private class LocationReceivedEvent {

    }
}

package com.lcjian.cloudlocation.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.bumptech.glide.Glide;
import com.franmontiel.localechanger.utils.ActivityRecreationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.Global;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.network.entity.MonitorInfo;
import com.lcjian.cloudlocation.ui.base.BaseFragment;
import com.lcjian.cloudlocation.ui.device.DeviceInfoActivity;
import com.lcjian.cloudlocation.ui.device.DevicesActivity;
import com.lcjian.cloudlocation.ui.device.GEOFenceListActivity;
import com.lcjian.cloudlocation.ui.device.HistoryPathActivity;
import com.lcjian.cloudlocation.ui.device.IconSettingActivity;
import com.lcjian.cloudlocation.ui.device.PanoramaActivity;
import com.lcjian.cloudlocation.ui.web.MessageSettingActivity;
import com.lcjian.cloudlocation.ui.web.SendCommandActivity;
import com.lcjian.cloudlocation.util.DimenUtils;
import com.lcjian.cloudlocation.util.MapUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

public class HomeContentFragmentGoogle extends BaseFragment implements SensorEventListener, View.OnClickListener, OnMapReadyCallback {

    ViewGroup mView;
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
    @BindView(R.id.iv_show_distance_2)
    ImageView iv_show_distance_2;
    @BindView(R.id.iv_go_to_device_location)
    ImageView iv_go_to_device_location;
    @BindView(R.id.iv_go_to_my_location)
    ImageView iv_go_to_my_location;
    @BindView(R.id.cv_go_to_map_navigation)
    ImageView cv_go_to_map_navigation;
    @BindView(R.id.iv_zoom_in)
    ImageView iv_zoom_in;
    @BindView(R.id.iv_zoom_out)
    ImageView iv_zoom_out;
    @BindView(R.id.tv_distance)
    TextView tv_distance;
    @BindView(R.id.btn_search_device)
    ConstraintLayout btn_search_device;
    @BindView(R.id.tv_go_to_history_path)
    TextView tv_go_to_history_path;
    @BindView(R.id.tv_go_to_fence_setting)
    TextView tv_go_to_fence_setting;
    @BindView(R.id.tv_go_to_icon_setting)
    TextView tv_go_to_icon_setting;
    @BindView(R.id.tv_go_to_device_info)
    TextView tv_go_to_device_info;
    @BindView(R.id.tv_go_to_device_list)
    TextView tv_go_to_device_list;
    @BindView(R.id.iv_go_to_command)
    ImageView iv_go_to_command;

    Unbinder unbinder;

    private GoogleMap mGMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private SensorManager mSensorManager;

    private Location mCurrentLocation;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccuracy;

    private List<Marker> mDeviceMakers;
    private Polyline mPolyline;

    private int mMapType = GoogleMap.MAP_TYPE_NORMAL;
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
    private Disposable mDisposableTest;

    private MonitorInfo.MonitorDevice mCurrentDevice;
    private MonitorInfo.MonitorDevice origin;
    private List<MonitorInfo.MonitorDevice> monitorDevices;

    private Activity mActivity;

    private InfoWindowManager mInfoWindowManager;
    private InfoWindow mInfoWindow;

    private String mCurrentUserId;

    public static HomeContentFragmentGoogle newInstance(MonitorInfo.MonitorDevice origin) {
        HomeContentFragmentGoogle fragment = new HomeContentFragmentGoogle();
        Bundle args = new Bundle();
        args.putSerializable("origin", origin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            origin = (MonitorInfo.MonitorDevice) getArguments().getSerializable("origin");
        }
        mDeviceMakers = new ArrayList<>();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = (ViewGroup) inflater.inflate(R.layout.fragment_homet_contentt_google, container, false);
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
        iv_show_distance_2.setOnClickListener(this);

        tv_go_to_history_path.setOnClickListener(this);
        tv_go_to_fence_setting.setOnClickListener(this);
        tv_go_to_icon_setting.setOnClickListener(this);
        tv_go_to_device_info.setOnClickListener(this);
        tv_go_to_device_list.setOnClickListener(this);
        iv_go_to_command.setOnClickListener(this);

        if (origin != null || getSignInInfo().userInfo == null) {
            btn_search_device.setVisibility(View.GONE);
        }

        MapInfoWindowFragment mapFragment = (MapInfoWindowFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mInfoWindowManager = mapFragment.infoWindowManager();
        mInfoWindowManager.setContainerSpec(new InfoWindowManager.ContainerSpecification(null));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
        mSettingsClient = LocationServices.getSettingsClient(mActivity);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mCurrentLocation = locationResult.getLastLocation();
                mCurrentLat = mCurrentLocation.getLatitude();
                mCurrentLon = mCurrentLocation.getLongitude();
                mCurrentAccuracy = mCurrentLocation.getAccuracy();

                subjectLocation.onNext(new LocationReceivedEvent());
            }
        };
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest).build();

        mDisposableTest = Single.defer(() -> Single.just(testUrl("https://www.google.com")))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    if (!aBoolean) Timber.e("Google %s", "failed");
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGMap = googleMap;

        // 地图设置
        mGMap.getUiSettings().setZoomControlsEnabled(false);
        mGMap.getUiSettings().setCompassEnabled(false);
        mGMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGMap.setMapType(mMapType);
        mGMap.setMyLocationEnabled(true);

        mGMap.setOnMarkerClickListener(marker -> {
            MonitorInfo.MonitorDevice device = (MonitorInfo.MonitorDevice) marker.getTag();
            assert device != null;
            if (mCurrentDevice != null && TextUtils.equals(device.id, mCurrentDevice.id)) {
                mShowWindow = !mShowWindow;
            } else {
                mShowWindow = true;
            }

            for (Marker m : mDeviceMakers) {
                m.setZIndex(0);
            }
            marker.setZIndex(1.0f);

            mCurrentDevice = device;
            mUserInfoSp.edit().putString("current_device", new Gson().toJson(mCurrentDevice)).apply();
            mGMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng))));
            subjectDeviceChange.onNext(new CurrentDeviceChangeEvent());
            setupInfoWindowVisibility();
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);

        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(mActivity, locationSettingsResponse ->
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper()))
                .addOnFailureListener(mActivity, e -> {
                });

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
                        Single.just(mCurrentDevice),
                        mRestAPI.cloudService().getTrack(Long.parseLong(mCurrentDevice.id), mCurrentDevice.model, mUserInfoSp.getString("sign_in_map", "Google")),
                        mRestAPI.cloudService().getAddressByLatLng(mCurrentDevice.lat, mCurrentDevice.lng, mUserInfoSp.getString("sign_in_map", mUserInfoSp.getString("sign_in_map", "Google"))),
                        (cDevice, monitorDevice, address) -> {
                            monitorDevice.address = address.address;
                            monitorDevice.id = cDevice.id;
                            monitorDevice.name = cDevice.name;
                            monitorDevice.model = cDevice.model;
                            return monitorDevice;
                        })
                        .toObservable())
                .publish(selector -> Observable.zip(selector, selector.map(this::gn), Pair::create))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    assert pair.first != null;
                    assert pair.second != null;
                    MonitorInfo.MonitorDevice monitorDevice = pair.first;
                    BitmapDescriptor bitmapDescriptor = pair.second;

                    mCurrentDevice = monitorDevice;
                    Marker currentDeviceMaker = null;
                    for (Marker marker : mDeviceMakers) {
                        MonitorInfo.MonitorDevice device = (MonitorInfo.MonitorDevice) marker.getTag();
                        if (TextUtils.equals(device.id, mCurrentDevice.id)) {
                            currentDeviceMaker = marker;
                            break;
                        }
                    }
                    if (currentDeviceMaker != null) {
                        mDeviceMakers.remove(currentDeviceMaker);
                        currentDeviceMaker.remove();
                        mDeviceMakers.add(mapAddDeviceMarker(mCurrentDevice, bitmapDescriptor));
                    }
                    if (mChoose) {
                        mGMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
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
                                            mUserInfoSp.getString("sign_in_map", "Google"))
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
                            mCurrentUserId = TextUtils.isEmpty(Global.CURRENT_USER_ID) ? getSignInInfo().userInfo.userID : Global.CURRENT_USER_ID;
                            return mRestAPI.cloudService().monitorDevices(Long.parseLong(mCurrentUserId),
                                    mUserInfoSp.getString("sign_in_map", "Google"),
                                    mUserInfoSp.getString("sign_in_name", ""),
                                    mUserInfoSp.getString("sign_in_name_pwd", "")).toObservable();
                        }
                    })
                    .publish(selector -> Observable.zip(selector, selector.map(monitorInfo -> {
                        Map<MonitorInfo.MonitorDevice, BitmapDescriptor> descriptors = new HashMap<>();
                        for (MonitorInfo.MonitorDevice device : monitorInfo.devices) {
                            descriptors.put(device, gn(device));
                        }
                        return descriptors;
                    }), Pair::create))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(pair -> {
                        assert pair.first != null;
                        assert pair.second != null;
                        for (Marker marker : mDeviceMakers) {
                            marker.remove();
                        }
                        monitorDevices = pair.first.devices;

                        for (MonitorInfo.MonitorDevice device : pair.first.devices) {
                            mDeviceMakers.add(mapAddDeviceMarker(device, pair.second.get(device)));
                        }

                        mView.postDelayed(() -> {
                            if (mCurrentDevice == null && pair.first.devices != null && !pair.first.devices.isEmpty()) {
                                mCurrentDevice = getLastDevice(pair.first.devices);
                            if (mCurrentDevice == null) {
                                mCurrentDevice = pair.first.devices.get(0);
                            }
                            mGMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                    .target(new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)))
                                    .zoom(18)
                                    .build()));
                            subjectDeviceChange.onNext(new CurrentDeviceChangeEvent());
                            setupDistanceVisibility();
                            startRefresh();
                        }
                        }, 100);
                    }, throwable -> {
                    });
        } else {
            mView.post(() -> {
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
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mDisposableTest.dispose();
        mGMap.setMyLocationEnabled(false);
        unbinder.unbind();
        mView = null;
        super.onDestroyView();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
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
                mGMap.animateCamera(CameraUpdateFactory.zoomIn());
                break;
            case R.id.iv_zoom_out:
                mGMap.animateCamera(CameraUpdateFactory.zoomOut());
                break;
            case R.id.iv_go_to_my_location:
                if (mCurrentDevice != null) {
                    mGMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng))));
                }
                break;
            case R.id.iv_go_to_device_location:
                mGMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mCurrentLat, mCurrentLon)));
                break;
            case R.id.iv_show_distance:
                if (mCurrentDevice != null) {
                    if (MapUtils.haveGoogleMap(v.getContext())) {
                        MapUtils.openGoogleMap(v.getContext(),
                                new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)));
                    } else {
                        Toast.makeText(App.getInstance(), R.string.no_map_app, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.iv_show_distance_2:
                if (mCurrentDevice != null) {
                    mShowWindow = !mShowWindow;
                    setupInfoWindowVisibility();
                }
                break;
            case R.id.cv_change_map_layer:
                if (mMapType == GoogleMap.MAP_TYPE_NORMAL) {
                    mMapType = GoogleMap.MAP_TYPE_SATELLITE;
                } else {
                    mMapType = GoogleMap.MAP_TYPE_NORMAL;
                }
                mGMap.setMapType(mMapType);
                break;
            case R.id.cv_change_to_panorama:
                if (mCurrentDevice != null) {
                    startActivity(new Intent(v.getContext(), PanoramaActivity.class)
                            .putExtra("longitude", Double.parseDouble(mCurrentDevice.lng))
                            .putExtra("latitude", Double.parseDouble(mCurrentDevice.lat)));
                }
                break;
            case R.id.cv_go_to_map_navigation:
                mShowDistance = !mShowDistance;
                setupDistanceVisibility();
                if (mShowDistance) {
                    LatLng start = new LatLng(mCurrentLat, mCurrentLon);
                    LatLng end = new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng));
                    mGMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder()
                            .include(start)
                            .include(end)
                            .build(), 50));
                }
                break;
            case R.id.tv_go_to_history_path:
                if (mCurrentDevice != null) {
                    startActivity(new Intent(v.getContext(), HistoryPathActivity.class).putExtra("monitor_device", mCurrentDevice));
                }
                break;
            case R.id.tv_go_to_fence_setting:
                if (mCurrentDevice != null) {
                    startActivity(new Intent(v.getContext(), GEOFenceListActivity.class).putExtra("monitor_device", mCurrentDevice));
                }
                break;
            case R.id.tv_go_to_icon_setting:
                if (mCurrentDevice != null) {
                    startActivity(new Intent(v.getContext(), IconSettingActivity.class).putExtra("monitor_device", mCurrentDevice));
                }
                break;
            case R.id.tv_go_to_device_info:
                if (mCurrentDevice != null) {
                    startActivity(new Intent(v.getContext(), DeviceInfoActivity.class).putExtra("device_id", mCurrentDevice.id));
                }
                break;
            case R.id.tv_go_to_device_list:
                startActivityForResult(new Intent(v.getContext(), DevicesActivity.class), 1000);
                break;
            case R.id.iv_go_to_command:
                if (mCurrentDevice != null) {
                    startActivity(new Intent(v.getContext(), SendCommandActivity.class).putExtra("device_id", mCurrentDevice.id));
                }
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
                if (!TextUtils.equals(mCurrentUserId, Global.CURRENT_USER_ID)) {
                    ActivityRecreationHelper.recreate(getActivity(), true);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private BitmapDescriptor gn(MonitorInfo.MonitorDevice device) {
        View mMarkerView = LayoutInflater.from(mView.getContext()).inflate(R.layout.device_maker_item, mView, false);
        ImageView iv_device_icon = mMarkerView.findViewById(R.id.iv_device_icon);
        TextView tv_device_name = mMarkerView.findViewById(R.id.tv_device_name);

        try {
            Bitmap bitmap = Glide.with(iv_device_icon)
                    .asBitmap().load("file:///android_asset/icon/" + device.icon)
                    .placeholder(R.drawable.pos_min)
                    .circleCrop()
                    .submit()
                    .get();
            iv_device_icon.setImageBitmap(bitmap);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (TextUtils.equals("3", device.status.split("-")[0])) {
            iv_device_icon.setBackgroundResource(R.drawable.shape_offline);
            tv_device_name.setTextColor(0xffbbbbbb);
        } else if (TextUtils.equals("1", device.status.split("-")[0])) {
            iv_device_icon.setBackgroundResource(R.drawable.shape_sport);
            tv_device_name.setTextColor(0xff0000ff);
        } else {
            iv_device_icon.setBackgroundResource(R.drawable.shape_static);
            tv_device_name.setTextColor(0xff00ff00);
        }
        tv_device_name.setText(device.name);

        return BitmapDescriptorFactory.fromBitmap(com.baidu.mapapi.map.BitmapDescriptorFactory.fromView(mMarkerView).getBitmap());
    }

    private Marker mapAddDeviceMarker(MonitorInfo.MonitorDevice device, BitmapDescriptor bitmapDescriptor) {
        MarkerOptions makerOption = new MarkerOptions()
                .position(new LatLng(Double.parseDouble(device.lat), Double.parseDouble(device.lng)))
                .icon(bitmapDescriptor)
                .anchor(0.5f, 0.33f);
        Marker marker = mGMap.addMarker(makerOption);
        marker.setTag(device);
        return marker;
    }

    private void setupInfoWindowVisibility() {
        if (!mShowWindow) {
            if (mInfoWindow != null) {
                mInfoWindowManager.hide(mInfoWindow);
            }
            return;
        }

        mInfoWindow = new InfoWindow(
                new LatLng(Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng)),
                new InfoWindow.MarkerSpecification(0, (int) DimenUtils.dipToPixels(16, mView.getContext())),
                InfoWindowFragment.newInstance(origin, mCurrentDevice));
        mInfoWindowManager.show(mInfoWindow, false);
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
            mPolyline = mGMap.addPolyline(new PolylineOptions()
                    .width(10)
                    .color(Color.GREEN)
                    .addAll(Arrays.asList(start, end)));

            float[] results = new float[1];
            Location.distanceBetween(mCurrentLat, mCurrentLon, Double.parseDouble(mCurrentDevice.lat), Double.parseDouble(mCurrentDevice.lng), results);

            String s = getString(R.string.apart) + (results[0] > 1000 ? new DecimalFormat("0.00").format(results[0] / 1000) + "Km" : results[0] + "m");
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

    private static boolean testUrl(String urlString) {
        URL url;
        InputStream in = null;
        try {
            url = new URL(urlString);
            in = url.openStream();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class CurrentDeviceChangeEvent {

    }

    private class LocationReceivedEvent {

    }


    public static class InfoWindowFragment extends BaseFragment {

        private View mView;

        @BindView(R.id.tv_device_time)
        TextView tv_device_time;
        @BindView(R.id.tv_device_status)
        TextView tv_device_status;
        @BindView(R.id.tv_device_info)
        TextView tv_device_info;
        @BindView(R.id.iv_device_direct)
        ImageView iv_device_direct;
        @BindView(R.id.tv_device_direct)
        TextView tv_device_direct;
        @BindView(R.id.tv_device_address)
        TextView tv_device_address;

        Unbinder unbinder;

        private MonitorInfo.MonitorDevice mCurrentDevice;
        private MonitorInfo.MonitorDevice origin;

        static InfoWindowFragment newInstance(MonitorInfo.MonitorDevice origin,
                                              MonitorInfo.MonitorDevice currentDevice) {
            InfoWindowFragment fragment = new InfoWindowFragment();
            Bundle args = new Bundle();
            args.putSerializable("origin", origin);
            args.putSerializable("current_device", currentDevice);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                origin = (MonitorInfo.MonitorDevice) getArguments().getSerializable("origin");
                mCurrentDevice = (MonitorInfo.MonitorDevice) getArguments().getSerializable("current_device");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mView = inflater.inflate(R.layout.window_device_info, container, false);
            unbinder = ButterKnife.bind(this, mView);
            return mView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            String strStatus;
            switch (mCurrentDevice.status.split("-")[0]) {
                case "0":
                    strStatus = getString(R.string.un_used);
                    tv_device_status.setBackgroundResource(R.drawable.shape_status_offline);
                    break;
                case "1":
                    strStatus = getString(R.string.moving);
                    tv_device_status.setBackgroundResource(R.drawable.shape_status_sport);
                    break;
                case "2":
                    strStatus = getString(R.string.status_static);
                    tv_device_status.setBackgroundResource(R.drawable.shape_status_static);
                    break;
                case "3":
                    strStatus = getString(R.string.offline);
                    tv_device_status.setBackgroundResource(R.drawable.shape_status_offline);
                    break;
                case "4":
                    strStatus = getString(R.string.arrears);
                    tv_device_status.setBackgroundResource(R.drawable.shape_status_offline);
                    break;
                default:
                    strStatus = getString(R.string.un_used);
                    tv_device_status.setBackgroundResource(R.drawable.shape_status_offline);
                    break;
            }
            double course = Double.parseDouble(mCurrentDevice.course);
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

            String[] a = mCurrentDevice.status.split("-");
            String detail = (TextUtils.equals("1", mCurrentDevice.isGPS) ? "GPS "
                    : TextUtils.equals("2", mCurrentDevice.isGPS) ? "LBS "
                    : TextUtils.equals("3", mCurrentDevice.isGPS) ? "WIFI " : "") + (a.length > 1 ? a[1] : "") ;
            tv_device_time.setText(mCurrentDevice.positionTime);
            tv_device_status.setText(strStatus);
            tv_device_info.setText(detail);
            tv_device_direct.setText(courseStatus);
            tv_device_address.setText((TextUtils.isEmpty(mCurrentDevice.address) ? "" : mCurrentDevice.address));
            iv_device_direct.setRotation((float) course);
        }

        @Override
        public void onDestroyView() {
            unbinder.unbind();
            super.onDestroyView();
        }
    }
}

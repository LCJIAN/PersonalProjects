package com.lcjian.cloudlocation.ui.device;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Circle;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.network.entity.GEOFences;
import com.lcjian.cloudlocation.data.network.entity.MonitorInfo;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.util.Spans;

import java.util.concurrent.TimeUnit;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.TransitionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GEOFenceEditActivity extends BaseActivity implements SensorEventListener, View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;
    @BindView(R.id.cl_geo_fence_edit)
    ConstraintLayout cl_geo_fence_edit;
    @BindView(R.id.v_map)
    MapView v_map;
    @BindView(R.id.sb_fence_radius)
    SeekBar sb_fence_radius;
    @BindView(R.id.tv_fence_radius_geo_edit)
    TextView tv_fence_radius_geo_edit;
    @BindView(R.id.cv_change_map_layer_geo)
    ImageView cv_change_map_layer_geo;
    @BindView(R.id.cv_change_to_panorama_geo)
    ImageView cv_change_to_panorama_geo;
    @BindView(R.id.iv_switch_fence_center_location)
    ImageView iv_switch_fence_center_location;
    @BindView(R.id.iv_search_fence_location)
    ImageView iv_search_fence_location;
    @BindView(R.id.iv_zoom_in)
    ImageView iv_zoom_in;
    @BindView(R.id.iv_zoom_out)
    ImageView iv_zoom_out;
    @BindView(R.id.et_fence_name)
    EditText et_fence_name;
    @BindView(R.id.rb_fence_type_home)
    RadioButton rb_fence_type_home;
    @BindView(R.id.rb_fence_type_company)
    RadioButton rb_fence_type_company;
    @BindView(R.id.rb_fence_type_other)
    RadioButton rb_fence_type_other;
    @BindView(R.id.rg_fence_type)
    RadioGroup rg_fence_type;
    @BindView(R.id.chb_in_fence_remind)
    CheckBox chb_in_fence_remind;
    @BindView(R.id.chb_out_fence_remind)
    CheckBox chb_out_fence_remind;

    private MonitorInfo.MonitorDevice mMonitorDevice;
    private GEOFences.GEOFence mGEOFence;

    private BaiduMap mBMap;
    private LocationClient mLocClient;
    private SensorManager mSensorManager;

    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccuracy;
    private MyLocationData mLocData;

    private double mFenceLat = 0.0;
    private double mFenceLon = 0.0;
    private Overlay mMarker;
    private Circle mCircle;

    private int mMapType = BaiduMap.MAP_TYPE_NORMAL;
    private int mFenceCenterLocationType = 0;

    private Disposable mDisposable;
    private Disposable mDisposable2;
    private Disposable mDisposable3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence_edit);
        ButterKnife.bind(this);
        mMonitorDevice = (MonitorInfo.MonitorDevice) getIntent().getSerializableExtra("monitor_device");
        if (getIntent().getSerializableExtra("geo_fence") != null) {
            mGEOFence = (GEOFences.GEOFence) getIntent().getSerializableExtra("geo_fence");
        }

        tv_title.setText(R.string.edit_geo_fence);
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.bjwl_bc);
        rb_fence_type_home.setText(new Spans()
                .append("*", new ImageSpan(this, R.drawable.bjwl_j))
                .append(" ")
                .append(getString(R.string.home)));
        rb_fence_type_company.setText(new Spans()
                .append("*", new ImageSpan(this, R.drawable.bjwl_gs))
                .append(" ")
                .append(getString(R.string.company)));
        rb_fence_type_other.setText(new Spans()
                .append("*", new ImageSpan(this, R.drawable.bjwl_qt))
                .append(" ")
                .append(getString(R.string.other)));
        chb_in_fence_remind.setText(new Spans()
                .append("*", new ImageSpan(this, R.drawable.bjwl_jwlbj))
                .append(" ")
                .append(getString(R.string.in_fence_remind)));
        chb_out_fence_remind.setText(new Spans()
                .append("*", new ImageSpan(this, R.drawable.bjwl_cwlbj))
                .append(" ")
                .append(getString(R.string.out_fence_remind)));

        btn_nav_back.setOnClickListener(this);
        btn_nav_right.setOnClickListener(this);
        iv_switch_fence_center_location.setOnClickListener(this);
        cv_change_map_layer_geo.setOnClickListener(this);
        cv_change_to_panorama_geo.setOnClickListener(this);
        iv_zoom_in.setOnClickListener(this);
        iv_zoom_out.setOnClickListener(this);
        sb_fence_radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawCircle(sb_fence_radius.getProgress());
                String s = progress + "m";
                tv_fence_radius_geo_edit.setText(s);
                mRxBus.send(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                TransitionManager.beginDelayedTransition(cl_geo_fence_edit);
                tv_fence_radius_geo_edit.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tv_fence_radius_geo_edit.setVisibility(View.GONE);
                TransitionManager.beginDelayedTransition(cl_geo_fence_edit);
            }
        });

        if (mGEOFence == null) {
            sb_fence_radius.setProgress(1000);
        } else {
            sb_fence_radius.setProgress(Double.valueOf(mGEOFence.radius).intValue());
            et_fence_name.setText(mGEOFence.fenceName);
            rg_fence_type.check(TextUtils.equals("0", mGEOFence.FenceType) ? R.id.rb_fence_type_home
                    : (TextUtils.equals("1", mGEOFence.FenceType) ? R.id.rb_fence_type_company : R.id.rb_fence_type_other));
            chb_in_fence_remind.setChecked(TextUtils.equals("1", mGEOFence.Entry));
            chb_out_fence_remind.setChecked(TextUtils.equals("1", mGEOFence.Exit));
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 地图设置
        v_map.showZoomControls(false);
        mBMap = v_map.getMap();
        mBMap.getUiSettings().setCompassEnabled(false);
        mBMap.setMapType(mMapType);
        mBMap.setMyLocationEnabled(true);
        mBMap.setMyLocationConfiguration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));
        mBMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
                LatLng ll = marker.getPosition();
                mFenceLat = ll.latitude;
                mFenceLon = ll.longitude;
                drawCircle(sb_fence_radius.getProgress());
                mBMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(ll));
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }

            @Override
            public void onMarkerDragStart(Marker marker) {

            }
        });

        mDisposable = mRxBus.asFlowable()
                .filter(o -> o instanceof LatLng)
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    LatLng ll = (LatLng) o;
                    mFenceLat = ll.latitude;
                    mFenceLon = ll.longitude;

                    drawMarker();
                    drawCircle(sb_fence_radius.getProgress());
                    animateToTarget(sb_fence_radius.getProgress());
                });
        mDisposable2 = mRxBus.asFlowable()
                .filter(o -> o instanceof Integer)
                .debounce(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> animateToTarget((Integer) o));

        mRxBus.send(new LatLng(Double.parseDouble(mMonitorDevice.lat), Double.parseDouble(mMonitorDevice.lng)));
        if (mGEOFence != null) {
            mRxBus.send(new LatLng(Double.parseDouble(mGEOFence.lat), Double.parseDouble(mGEOFence.lng)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        v_map.onResume();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);

        mLocClient = new LocationClient(this);
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
            }
        });
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(10 * 1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    @Override
    protected void onPause() {
        mLocClient.stop();
        v_map.onPause();
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        mDisposable2.dispose();
        if (mDisposable3 != null) {
            mDisposable3.dispose();
        }
        mBMap.setMyLocationEnabled(false);
        v_map.onDestroy();
        super.onDestroy();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nav_back:
                onBackPressed();
                break;
            case R.id.btn_nav_right:
                showProgress();
                mDisposable3 = mRestAPI.cloudService().saveGEOFence(getSignInInfo().userInfo == null ? 0 : Long.parseLong(getSignInInfo().userInfo.userID),
                        Long.parseLong(mMonitorDevice.id),
                        mGEOFence == null ? 0 : Long.parseLong(mGEOFence.geofenceID),
                        et_fence_name.getEditableText().toString(),
                        rb_fence_type_home.isChecked() ? 0 : (rb_fence_type_company.isChecked() ? 1 : 2),
                        mFenceLat,
                        mFenceLon,
                        sb_fence_radius.getProgress(),
                        chb_in_fence_remind.isChecked() ? 1 : 0,
                        chb_out_fence_remind.isChecked() ? 1 : 0,
                        "none",
                        mUserInfoSp.getString("sign_in_map", "Google"))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(state -> {
                                    hideProgress();
                                    if (TextUtils.equals("0", state.state)) {
                                        Toast.makeText(App.getInstance(), R.string.save_failed, Toast.LENGTH_SHORT).show();
                                    } else {
                                        finish();
                                    }
                                },
                                throwable -> hideProgress());
                break;
            case R.id.iv_zoom_in:
                mBMap.animateMapStatus(MapStatusUpdateFactory.zoomIn());
                break;
            case R.id.iv_zoom_out:
                mBMap.animateMapStatus(MapStatusUpdateFactory.zoomOut());
                break;
            case R.id.iv_switch_fence_center_location:
                if (mFenceCenterLocationType == 0) {
                    mFenceCenterLocationType = 1;
                } else {
                    mFenceCenterLocationType = 0;
                }
                if (mFenceCenterLocationType == 0) {
                    mRxBus.send(new LatLng(mCurrentLat, mCurrentLon));
                    Toast.makeText(App.getInstance(),  R.string.my_location, Toast.LENGTH_SHORT).show();
                } else {
                    mRxBus.send(new LatLng(Double.parseDouble(mMonitorDevice.lat), Double.parseDouble(mMonitorDevice.lng)));
                    Toast.makeText(App.getInstance(),  R.string.device_location, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.cv_change_map_layer_geo:
                if (mMapType == BaiduMap.MAP_TYPE_NORMAL) {
                    mMapType = BaiduMap.MAP_TYPE_SATELLITE;
                } else {
                    mMapType = BaiduMap.MAP_TYPE_NORMAL;
                }
                mBMap.setMapType(mMapType);
                break;
            case R.id.cv_change_to_panorama_geo:
                if (mFenceLat != 0.0d) {
                    startActivity(new Intent(v.getContext(), PanoramaActivity.class)
                            .putExtra("longitude", mFenceLon)
                            .putExtra("latitude", mFenceLat));
                }
                break;
            default:
                break;
        }
    }

    private void drawCircle(int radius) {
        if (mBMap == null) {
            return;
        }
        if (mCircle != null) {
            mCircle.remove();
            mCircle = null;
        }
        mCircle = (Circle) mBMap.addOverlay(new CircleOptions()
                .fillColor(0x384d73b3)
                .stroke(new Stroke(3, 0x784d73b3))
                .center(new LatLng(mFenceLat, mFenceLon))
                .zIndex(0)
                .radius(radius));
    }

    private void animateToTarget(int radius) {
        mBMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder()
                .target(new LatLng(mFenceLat, mFenceLon))
                .zoom((float) calZoomLevel(radius))
                .build()));
    }

    private void drawMarker() {
        if (mBMap == null) {
            return;
        }
        if (mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
        LatLng latLng = new LatLng(mFenceLat, mFenceLon);
        OverlayOptions makerOptionStart = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bjwl_szx))
                .draggable(true)
                .zIndex(1)
                .anchor(0.5f, 0.5f);
        mMarker = mBMap.addOverlay(makerOptionStart);
    }

    private double calZoomLevel(float radius) {

        // Equators length
        double equatorLength = 40075004.0;

        double distance = radius * 2;

        int screenSize = v_map.getWidth();

        // The meters per pixel required to show the whole area the user might be located in
        double requiredMpp = distance / screenSize;

        // Calculate the zoom level
        return ((Math.log(equatorLength / (256 * requiredMpp))) / Math.log(2));
    }
}

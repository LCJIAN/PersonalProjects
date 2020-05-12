package com.winside.lighting.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.winside.lighting.App;
import com.winside.lighting.GlideApp;
import com.winside.lighting.R;
import com.winside.lighting.WMapView;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.data.network.entity.Device;
import com.winside.lighting.data.network.entity.DeviceControl;
import com.winside.lighting.data.network.entity.Region;
import com.winside.lighting.data.network.entity.RegionFloorPlanData;
import com.winside.lighting.mesh.WTidGenerator;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.device.DeviceGatewayActivity;
import com.winside.lighting.ui.device.DeviceLightActivity;
import com.winside.lighting.ui.device.DeviceSensorActivity;
import com.winside.lighting.ui.device.DeviceSwitchActivity;
import com.winside.lighting.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kz.mobdev.mapview.library.MapViewListener;
import kz.mobdev.mapview.library.layers.BitmapLayer;

public class ManagementFragment extends BaseFragment {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.srl_floor_plan)
    SwipeRefreshLayout srl_floor_plan;
    @BindView(R.id.fl_map)
    FrameLayout fl_map;

    @BindView(R.id.ll_control_all)
    LinearLayout ll_control_all;
    @BindView(R.id.switch_device_all)
    SwitchCompat switch_device_all;
    @BindView(R.id.seek_bar_color_temperature_all)
    SeekBar seek_bar_color_temperature_all;
    @BindView(R.id.seek_bar_light_all)
    SeekBar seek_bar_light_all;

    private WMapView v_map;

    private Unbinder unbinder;

    private Disposable mDisposable;
    private Disposable mDisposableBind;
    private Disposable mDisposableSend;
    private Disposable mDisposableD;

    private FloorPlanFilterFragment mFilterFragment;

    private Region mRegion;
    private RegionFloorPlanData mRegionFloorPlanData;

    private Device mDevice;

    private Long mClickedCoordinateId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_management, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_navigation_title.setText(R.string.action_management);
        tv_navigation_title.setOnClickListener(v -> {
            if (mFilterFragment.isHidden()) {
                showFilter();
            } else {
                hideFilter();
            }
        });
        srl_floor_plan.setColorSchemeResources(R.color.colorPrimary);
        srl_floor_plan.setOnRefreshListener(this::setupFloorPlan);
        srl_floor_plan.setEnabled(false);

        switch_device_all.setOnClickListener(v -> {
            DeviceControl deviceControl = new DeviceControl();
            deviceControl.option = "DeviceControl";
            deviceControl.NetID = "fc539e438778";
            deviceControl.multicast = Integer.parseInt(mDevice.fullMultiCast.split(",")[0]);
            deviceControl.data = new DeviceControl.Data();
            deviceControl.data.opcode = "8202";
            deviceControl.data.params = (switch_device_all.isChecked() ? "01" : "00")
                    + Utils.byteToHexString(WTidGenerator.getInstanceFor(Utils.formatMac(mDevice.mac)).getAndIncrement());
            sendToDevice(new Gson().toJson(deviceControl));
        });
        seek_bar_color_temperature_all.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            private boolean fromUser;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.fromUser = fromUser;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fromUser) {
                    DeviceControl deviceControl = new DeviceControl();
                    deviceControl.option = "DeviceControl";
                    deviceControl.NetID = "fc539e438778";
                    deviceControl.multicast = Integer.parseInt(mDevice.fullMultiCast.split(",")[0]);
                    deviceControl.data = new DeviceControl.Data();
                    deviceControl.data.opcode = "825E";
                    deviceControl.data.params = Utils.hexByteToHexString(Utils.short_to_bb_le((short) seek_bar_light_all.getProgress())) // Lightness
                            + Utils.hexByteToHexString(Utils.short_to_bb_le((short) (seekBar.getProgress() + 0x0320))) // Temperature
                            + Utils.hexByteToHexString(Utils.short_to_bb_le((short) 0)) // DeUV
                            + Utils.byteToHexString(WTidGenerator.getInstanceFor(Utils.formatMac(mDevice.mac)).getAndIncrement()); // Tid
                    sendToDevice(new Gson().toJson(deviceControl));
                }
            }
        });
        seek_bar_light_all.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            private boolean fromUser;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.fromUser = fromUser;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fromUser) {
                    DeviceControl deviceControl = new DeviceControl();
                    deviceControl.option = "DeviceControl";
                    deviceControl.NetID = "fc539e438778";
                    deviceControl.multicast = Integer.parseInt(mDevice.fullMultiCast.split(",")[0]);
                    deviceControl.data = new DeviceControl.Data();
                    deviceControl.data.opcode = "824C";
                    deviceControl.data.params = Utils.hexByteToHexString(Utils.short_to_bb_le((short) seekBar.getProgress()))
                            + Utils.byteToHexString(WTidGenerator.getInstanceFor(Utils.formatMac(mDevice.mac)).getAndIncrement());
                    sendToDevice(new Gson().toJson(deviceControl));
                }
            }
        });

        mFilterFragment = new FloorPlanFilterFragment()
                .setOnRegionSelectedListener((building, floor, region) -> {
                    mRegion = region;
                    tv_navigation_title.setText(region.name);
                    hideFilter();
                    setupFloorPlan();
                });

        getChildFragmentManager().beginTransaction().add(R.id.fl_fragment_container, mFilterFragment).commit();
        showFilter();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPreferencesDataSource.getNeedRefresh() && mRegion != null) {
            setupFloorPlan();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableBind != null) {
            mDisposableBind.dispose();
        }
        if (mDisposableSend != null) {
            mDisposableSend.dispose();
        }
        if (mDisposableD != null) {
            mDisposableD.dispose();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                bindPosition(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void hideFilter() {
        getChildFragmentManager().beginTransaction().hide(mFilterFragment).commit();
        tv_navigation_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.rotate_arrow_up, 0);
    }

    private void showFilter() {
        getChildFragmentManager().beginTransaction().show(mFilterFragment).commit();
        tv_navigation_title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
    }

    private void setupFloorPlan() {
        srl_floor_plan.post(() -> srl_floor_plan.setRefreshing(true));
        ll_control_all.setVisibility(View.INVISIBLE);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = RestAPI.getInstance().lightingService().getRegionData(mRegion.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            srl_floor_plan.post(() -> srl_floor_plan.setRefreshing(false));
                            if (responseData.code == 1000) {
                                mDisposableD = RestAPI.getInstance().lightingService().getDevices(responseData.data.gatewayCoordinates.get(0).deviceId)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(listResponseData -> {
                                            mDevice = listResponseData.data.get(0);
                                            ll_control_all.setVisibility(View.VISIBLE);
                                        }, throwable -> {
                                        });
                                mRegionFloorPlanData = responseData.data;
                                SharedPreferencesDataSource.putNeedRefresh(false);
                                setupMap();
                            } else {
                                Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            srl_floor_plan.post(() -> srl_floor_plan.setRefreshing(false));
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

    private void setupMap() {
        if (v_map != null) {
            fl_map.removeView(v_map);
        }
        v_map = new WMapView(fl_map.getContext());
        v_map.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        v_map.setCompassButtonVisible(false);
        v_map.setZoomControlsVisible(false);
        v_map.setMaxZoom(5f);
        v_map.setScaleAndRotateTogether(true);
        v_map.setMapViewListener(new MapViewListener() {
            @Override
            public void onMapLoadSuccess() {
                for (RegionFloorPlanData.GatewayCoordinate g : mRegionFloorPlanData.gatewayCoordinates) {
                    for (RegionFloorPlanData.DeviceCoordinate d : g.deviceCoordinates) {
                        addDeviceMapLayer(d);
                    }
                    addGatewayMapLayer(g);
                }
            }

            @Override
            public void onMapLoadFail() {

            }
        });
        GlideApp.with(this)
                .asBitmap()
                .load(mRegionFloorPlanData.drawings)
                .into(new CustomTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        v_map.loadMap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
        fl_map.addView(v_map);
    }

    private void bindPosition(String deviceSN) {
        showProgress();
        if (mDisposableBind != null) {
            mDisposableBind.dispose();
        }
        mDisposableBind = RestAPI.getInstance().lightingService().bindPosition(mClickedCoordinateId, deviceSN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                            hideProgress();
                            if (objectResponseData.code == 1000) {
                                setupFloorPlan();
                                Toast.makeText(App.getInstance(), "绑定二维码成功", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(App.getInstance(), objectResponseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

    private void addGatewayMapLayer(RegionFloorPlanData.GatewayCoordinate g) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), getDeviceResource(g.deviceTypeId, g.deviceStatus));
        BitmapLayer bitmapLayer = new BitmapLayer(v_map, bmp);
        bitmapLayer.setLocation(new PointF(g.point[0].floatValue(), g.point[1].floatValue()));
        bitmapLayer.setOnBitmapClickListener(layer -> {
            mClickedCoordinateId = g.id;
            if (g.deviceId == null) {
                IntentIntegrator.forSupportFragment(this).initiateScan();
            } else {
                jumpTo(g.deviceTypeId, g.deviceTypeName, g.deviceId, g.id);
            }
        });
        v_map.addLayer(bitmapLayer);
        v_map.refresh();
    }

    private void addDeviceMapLayer(RegionFloorPlanData.DeviceCoordinate d) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), getDeviceResource(d.deviceTypeId, d.deviceStatus));
        BitmapLayer bitmapLayer = new BitmapLayer(v_map, bmp);
        bitmapLayer.setLocation(new PointF(d.point[0].floatValue(), d.point[1].floatValue()));
        bitmapLayer.setOnBitmapClickListener(layer -> {
            mClickedCoordinateId = d.id;
            if (d.deviceId == null) {
                IntentIntegrator.forSupportFragment(this).initiateScan();
            } else {
                jumpTo(d.deviceTypeId, d.deviceTypeName, d.deviceId, d.id);
            }
        });
        v_map.addLayer(bitmapLayer);
        v_map.refresh();
    }

    private void jumpTo(Integer deviceType, String deviceTypeName, Long deviceId, Long coordinateId) {
        if (deviceType == 1) {
            startActivity(new Intent(getContext(), DeviceGatewayActivity.class)
                    .putExtra("device_id", deviceId)
                    .putExtra("coordinate_id", coordinateId)
                    .putExtra("device_type_name", deviceTypeName));
        } else if (deviceType == 8) {
            startActivity(new Intent(getContext(), DeviceSensorActivity.class)
                    .putExtra("device_id", deviceId)
                    .putExtra("coordinate_id", coordinateId)
                    .putExtra("device_type_name", deviceTypeName));
        } else if (deviceType == 6) {
            startActivity(new Intent(getContext(), DeviceSwitchActivity.class)
                    .putExtra("device_id", deviceId)
                    .putExtra("coordinate_id", coordinateId)
                    .putExtra("device_type_name", deviceTypeName));
        } else if (deviceType == 2 || deviceType == 3 || deviceType == 4 || deviceType == 7) {
            startActivity(new Intent(getContext(), DeviceLightActivity.class)
                    .putExtra("device_id", deviceId)
                    .putExtra("coordinate_id", coordinateId)
                    .putExtra("device_type_name", deviceTypeName));
        }
    }

    private int getDeviceResource(Integer deviceType, String deviceStatus) {
        String prefix = "ic_floor_plan_";
        String suffix;

        if (deviceType == 1) {
            suffix = "gateway";
        } else if (deviceType == 8) {
            suffix = "sensor";
        } else if (deviceType == 6) {
            suffix = "switch";
        } else if (deviceType == 2 || deviceType == 3 || deviceType == 4 || deviceType == 7) {
            suffix = "light";
        } else {
            suffix = "unknown";
        }

        if (!TextUtils.isEmpty(deviceStatus)) {
            if (TextUtils.equals("unconf", deviceStatus)) {
                suffix = suffix + "_un_conf";
            } else if (TextUtils.equals("unreg", deviceStatus)) {
                suffix = suffix + "_un_reg";
            } else if (TextUtils.equals("online", deviceStatus)) {
                suffix = suffix + "_online";
            } else if (TextUtils.equals("offline", deviceStatus)) {
                suffix = suffix + "_offline";
            }
        }
        Context context = getContext();
        assert context != null;
        return getResources().getIdentifier(prefix + suffix, "drawable", context.getPackageName());
    }

    private void sendToDevice(String message) {
        showProgress();
        if (mDisposableSend != null) {
            mDisposableSend.dispose();
        }
        mDisposableSend = RestAPI.getInstance().lightingService().sendToDevice(mDevice.id, message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                    hideProgress();
                    if (objectResponseData.code == 1000) {
                        Toast.makeText(App.getInstance(), "操作成功", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(App.getInstance(), objectResponseData.message, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    hideProgress();
                    Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
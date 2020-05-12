package com.winside.lighting.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.data.network.entity.RegionFloorPlanData;
import com.winside.lighting.mesh.WTaskSwitchLight;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.base.SlimAdapter;
import com.winside.lighting.ui.device.DeviceGatewayActivity;
import com.winside.lighting.ui.device.DeviceLightActivity;
import com.winside.lighting.ui.device.DeviceSensorActivity;
import com.winside.lighting.ui.device.DeviceSwitchActivity;
import com.winside.lighting.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DevicesFragment extends BaseFragment {

    @BindView(R.id.srl_device)
    SwipeRefreshLayout srl_device;
    @BindView(R.id.rv_device)
    RecyclerView rv_device;

    private Unbinder unbinder;

    private SlimAdapter mAdapter;

    private Disposable mDisposable;

    private Disposable mDisposableS;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        srl_device.setColorSchemeResources(R.color.colorPrimary);
        srl_device.setOnRefreshListener(this::setupContent);
        rv_device.setHasFixedSize(true);
        rv_device.setLayoutManager(new GridLayoutManager(rv_device.getContext(), 2));

        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<RegionFloorPlanData.DeviceCoordinate>() {

            @Override
            public int onGetLayoutResource() {
                return R.layout.device_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<RegionFloorPlanData.DeviceCoordinate> viewHolder) {
                viewHolder
                        .clicked(v -> jumpTo(viewHolder.itemData.deviceTypeId, viewHolder.itemData.deviceTypeName,
                                viewHolder.itemData.deviceId, viewHolder.itemData.id))
                        .clicked(R.id.btn_switch_on_off, v -> switchLight(v.getContext(), viewHolder.itemData, ((CheckBox) v).isChecked()));
            }

            @Override
            public void onBind(RegionFloorPlanData.DeviceCoordinate data, SlimAdapter.SlimViewHolder<RegionFloorPlanData.DeviceCoordinate> viewHolder) {
                viewHolder.text(R.id.tv_device_name, data.deviceTypeName)
                        .text(R.id.tv_device_region, "办公区")
                        .image(R.id.iv_device, data.deviceTypeId == 2 ? R.drawable.ic_lighting_r : R.drawable.ic_sensor_r)
                        .visibility(R.id.btn_switch_on_off, data.deviceTypeId == 2 ? View.VISIBLE : View.INVISIBLE);
            }
        });
        rv_device.setAdapter(mAdapter);

        setupContent();
    }

    private void setupContent() {
        srl_device.post(() -> srl_device.setRefreshing(true));
        mDisposable = RestAPI.getInstance().lightingService().getProjects()
                .flatMap(listResponseData -> RestAPI.getInstance().lightingService().getBuildings(listResponseData.data.get(0).id))
                .flatMap(listResponseData -> RestAPI.getInstance().lightingService().getFloors(listResponseData.data.get(0).id))
                .flatMap(listResponseData -> RestAPI.getInstance().lightingService().getRegions(listResponseData.data.get(0).id))
                .flatMap(listResponseData -> RestAPI.getInstance().lightingService().getRegionData(listResponseData.data.get(0).id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            srl_device.post(() -> srl_device.setRefreshing(false));
                            if (responseData.code == 1000) {
                                RegionFloorPlanData.GatewayCoordinate g = responseData.data.gatewayCoordinates.get(0);
                                List<RegionFloorPlanData.DeviceCoordinate> devices = new ArrayList<>();
                                for (RegionFloorPlanData.DeviceCoordinate d : g.deviceCoordinates) {
                                    if (TextUtils.equals(d.deviceStatus, "offline")
                                            || TextUtils.equals(d.deviceStatus, "online")
                                            || TextUtils.equals(d.deviceStatus, "unreg")) {
                                        devices.add(d);
                                    }
                                }
                                mAdapter.updateData(devices);
                            } else {
                                Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            srl_device.post(() -> srl_device.setRefreshing(false));
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mDisposable.dispose();
    }

    @SuppressLint("StaticFieldLeak")
    private void switchLight(Context context, RegionFloorPlanData.DeviceCoordinate d, boolean switchOn) {
        showProgress();
        if (mDisposableS != null) {
            mDisposableS.dispose();
        }
        mDisposableS = RestAPI.getInstance().lightingService().getDeviceDetail(d.deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceResponseData -> {
                            hideProgress();
                            if (deviceResponseData.code == 1000) {
                                new WTaskSwitchLight(context, Utils.formatMac(deviceResponseData.data.mac),
                                        Utils.short_to_bb_le((short) Integer.parseInt(deviceResponseData.data.fullUniCast)), switchOn) {
                                    @Override
                                    protected void onPreExecute() {
                                        showProgress();
                                    }

                                    @Override
                                    protected void onPostExecute(Boolean aBoolean) {
                                        hideProgress();
                                        Toast.makeText(App.getInstance(), aBoolean ? "操作成功" : "操作失败", Toast.LENGTH_LONG).show();
                                    }
                                }
                                        .execute();
                            } else {
                                Toast.makeText(App.getInstance(), deviceResponseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
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
}
package com.winside.lighting.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.data.network.entity.RegionFloorPlanData;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.device.DevicesActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RegionsFragment extends BaseFragment {

    @BindView(R.id.cl_region_1)
    CardView cl_region_1;

    private TextView tv_region_name;
    private TextView tv_device_count;
    private ImageView iv_device_1;
    private ImageView iv_device_2;
    private ImageView iv_device_3;

    private Unbinder unbinder;

    private Disposable mDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_regions, container, false);
        unbinder = ButterKnife.bind(this, view);

        tv_region_name = cl_region_1.findViewById(R.id.tv_region_name);
        tv_device_count = cl_region_1.findViewById(R.id.tv_device_count);
        iv_device_1 = cl_region_1.findViewById(R.id.iv_device_1);
        iv_device_2 = cl_region_1.findViewById(R.id.iv_device_2);
        iv_device_3 = cl_region_1.findViewById(R.id.iv_device_3);
        iv_device_1.setVisibility(View.VISIBLE);
        iv_device_2.setVisibility(View.VISIBLE);
        iv_device_3.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        cl_region_1.setOnClickListener(v -> startActivity(new Intent(v.getContext(), DevicesActivity.class)));
        setupContent();
    }

    private void setupContent() {
        mDisposable = RestAPI.getInstance().lightingService().getProjects()
                .flatMap(listResponseData -> RestAPI.getInstance().lightingService().getBuildings(listResponseData.data.get(0).id))
                .flatMap(listResponseData -> RestAPI.getInstance().lightingService().getFloors(listResponseData.data.get(0).id))
                .flatMap(listResponseData -> RestAPI.getInstance().lightingService().getRegions(listResponseData.data.get(0).id))
                .flatMap(listResponseData -> RestAPI.getInstance().lightingService().getRegionData(listResponseData.data.get(0).id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
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
                                tv_region_name.setText("办公区");
                                tv_device_count.setText(devices.size() + "个设备");
                                iv_device_1.setImageResource(devices.get(0).deviceTypeId == 2 ? R.drawable.ic_lighting_r : R.drawable.ic_sensor_r);
                                iv_device_2.setImageResource(devices.get(1).deviceTypeId == 2 ? R.drawable.ic_lighting_r : R.drawable.ic_sensor_r);
                                iv_device_3.setImageResource(devices.get(2).deviceTypeId == 2 ? R.drawable.ic_lighting_r : R.drawable.ic_sensor_r);
                            } else {
                                Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mDisposable.dispose();
    }
}
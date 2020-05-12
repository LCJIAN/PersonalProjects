package com.winside.lighting.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winside.lighting.R;
import com.winside.lighting.data.db.entity.Device;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.base.SlimAdapter;
import com.winside.lighting.ui.device.AddDeviceActivity;
import com.winside.lighting.ui.device.DeviceActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DevicesFragment extends BaseFragment {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_right)
    ImageButton btn_right;
    @BindView(R.id.rv_device)
    RecyclerView rv_device;

    private Unbinder unbinder;

    private SlimAdapter mAdapter;

    private DevicesViewModel mDevicesViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mDevicesViewModel = ViewModelProviders.of(this).get(DevicesViewModel.class);

        tv_navigation_title.setText(R.string.action_device);
        btn_right.setVisibility(View.VISIBLE);
        btn_right.setOnClickListener(v -> startActivity(new Intent(v.getContext(), AddDeviceActivity.class)));

        rv_device.setHasFixedSize(true);
        rv_device.setLayoutManager(new GridLayoutManager(rv_device.getContext(), 2));
        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<Device>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.device_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<Device> viewHolder) {
                viewHolder
                        .clicked(v -> startActivity(new Intent(v.getContext(), DeviceActivity.class)
                                .putExtra("device_id", viewHolder.itemData.id)))
                        .clicked(R.id.btn_delete, v ->
                                new AlertDialog.Builder(v.getContext())
                                        .setTitle("是否确定删除？")
                                        .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                                        .setPositiveButton("确定", (dialog, which) -> {
                                            dialog.dismiss();
                                            mDevicesViewModel.deleteDevice(viewHolder.itemData);
                                        })
                                        .create()
                                        .show());
            }

            @Override
            public void onBind(Device data, SlimAdapter.SlimViewHolder<Device> viewHolder) {
                viewHolder.text(R.id.tv_device_name, data.name);
            }
        });
        rv_device.setAdapter(mAdapter);

        setupContent();
    }

    private void setupContent() {
        mDevicesViewModel.getDevices().observe(this, devices -> mAdapter.updateData(new ArrayList<>(devices)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

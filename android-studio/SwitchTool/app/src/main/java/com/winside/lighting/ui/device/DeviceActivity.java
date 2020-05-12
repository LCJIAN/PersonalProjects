package com.winside.lighting.ui.device;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winside.lighting.R;
import com.winside.lighting.data.db.entity.DeviceSwitchItemGroup;
import com.winside.lighting.data.db.entity.ItemAndGroup;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.ui.base.SlimAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceActivity extends BaseActivity {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.rv_device_switch_item)
    RecyclerView rv_device_switch_item;

    private DeviceViewModel mDeviceViewModel;

    private SlimAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ButterKnife.bind(this);

        mDeviceViewModel = ViewModelProviders
                .of(this,
                        new DeviceViewModel.Factory(getIntent().getLongExtra("device_id", 0)))
                .get(DeviceViewModel.class);

        tv_navigation_title.setText(R.string.device_detail);
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(v -> onBackPressed());

        rv_device_switch_item.setHasFixedSize(true);
        rv_device_switch_item.setLayoutManager(new LinearLayoutManager(rv_device_switch_item.getContext()));
        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<ItemAndGroup>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.device_switch_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<ItemAndGroup> viewHolder) {
                viewHolder.clicked(R.id.tv_add_to_group, v -> {

                    DeviceSwitchItemGroup group = null;
                    if (viewHolder.itemData.item.groupId != null) {
                        group = new DeviceSwitchItemGroup();
                        group.id = viewHolder.itemData.item.groupId;
                    }
                    new GroupPickerFragment()
                            .setData(viewHolder.itemData.item, group)
                            .show(getSupportFragmentManager(), "GroupPickerFragment");
                });
            }

            @Override
            public void onBind(ItemAndGroup data, SlimAdapter.SlimViewHolder<ItemAndGroup> viewHolder) {
                viewHolder.text(R.id.tv_device_switch_item_name,
                        data.item.name + (data.groupName == null ? "" : "(" + data.groupName + ")"))
                        .text(R.id.tv_add_to_group, data.groupName == null ? "编组" : "更改编组");
            }
        });
        rv_device_switch_item.setAdapter(mAdapter);

        setupContent();
    }

    private void setupContent() {
        mDeviceViewModel.getDeviceSwitchItems().observe(this, deviceSwitchItems -> mAdapter.updateData(new ArrayList<>(deviceSwitchItems)));
    }
}

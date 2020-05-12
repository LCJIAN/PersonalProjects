package com.winside.lighting.ui.test;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.winside.lighting.R;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.ui.base.SlimAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class Test2Activity extends BaseActivity {

    @BindView(R.id.srl_device)
    SwipeRefreshLayout srl_device;
    @BindView(R.id.rv_device)
    RecyclerView rv_device;

    private List<BluetoothDevice> mData;

    private SlimAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_2);
        ButterKnife.bind(this);

        srl_device.setOnRefreshListener(this::scan);

        rv_device.setHasFixedSize(true);
        rv_device.setLayoutManager(new LinearLayoutManager(this));

        mData = new ArrayList<>();
        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<BluetoothDevice>() {

            @Override
            public int onGetLayoutResource() {
                return R.layout.test_2_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<BluetoothDevice> viewHolder) {
                viewHolder.clicked(R.id.btn_sync, v -> {

                });
            }

            @Override
            public void onBind(BluetoothDevice data, SlimAdapter.SlimViewHolder<BluetoothDevice> viewHolder) {
                viewHolder.text(R.id.tv_device_name, data.getName());
            }
        });
        rv_device.setAdapter(mAdapter);
    }

    private void scan() {
        srl_device.post(() -> srl_device.setRefreshing(true));
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                if (!mData.contains(result.getDevice())) {
                    mData.add(result.getDevice());
                }
                mAdapter.updateData(mData);
            }
        };
        scanner.startScan(null, settings, scanCallback);

        srl_device.postDelayed(() -> {
            srl_device.setRefreshing(false);
            scanner.stopScan(scanCallback);
        }, 10000);
    }
}

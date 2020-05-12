package com.winside.lighting.ui.device;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.winside.lighting.App;
import com.winside.lighting.data.db.entity.Device;
import com.winside.lighting.data.db.entity.DeviceSwitchItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class AddDeviceViewModel extends ViewModel {

    private final MutableLiveData<List<BluetoothDevice>> mBluetoothDevices;
    private final MutableLiveData<List<BluetoothDevice>> mCheckedBluetoothDevices;

    private final MutableLiveData<Boolean> mSearching;

    private final BluetoothLeScannerCompat mScanner;
    private final ScanCallback mScanCallback;

    private final List<BluetoothDevice> mData;
    private final List<BluetoothDevice> mChecked;

    private Disposable mDisposable;

    public AddDeviceViewModel() {
        mBluetoothDevices = new MutableLiveData<>();
        mCheckedBluetoothDevices = new MutableLiveData<>();
        mSearching = new MutableLiveData<>();
        mData = new ArrayList<>();
        mChecked = new ArrayList<>();

        mScanner = BluetoothLeScannerCompat.getScanner();
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                BluetoothDevice d = result.getDevice();
                if (!TextUtils.isEmpty(d.getName()) && d.getName().startsWith("switch_4")) {
                    if (!mData.contains(d)) {
                        mData.add(d);
                        mBluetoothDevices.postValue(mData);
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                stopScan();
            }
        };
    }

    @Override
    protected void onCleared() {
        if (mDisposable != null) {
            mDisposable.dispose();
            stopScan();
        }
    }

    LiveData<List<BluetoothDevice>> getBluetoothDevices() {
        return mBluetoothDevices;
    }

    LiveData<List<BluetoothDevice>> getCheckedBluetoothDevices() {
        return mCheckedBluetoothDevices;
    }

    LiveData<Boolean> getSearching() {
        return mSearching;
    }

    void startScan() {
        mSearching.postValue(true);
        mData.clear();
        mChecked.clear();
        mBluetoothDevices.postValue(mData);
        mCheckedBluetoothDevices.postValue(mChecked);

        mScanner.startScan(
                null,
                new ScanSettings.Builder()
                        .setLegacy(false)
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setUseHardwareBatchingIfSupported(false)
                        .build(),
                mScanCallback);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = Single.just(true)
                .delay(5, TimeUnit.SECONDS)
                .subscribe(aBoolean -> stopScan());
    }

    void stopScan() {
        mSearching.postValue(false);
        mScanner.stopScan(mScanCallback);
    }

    void toggle(BluetoothDevice device) {
        if (mChecked.contains(device)) {
            mChecked.remove(device);
        } else {
            mChecked.add(device);
        }
        mCheckedBluetoothDevices.postValue(mChecked);
    }

    void saveDevices() {
        App.getInstance().getAppDatabase().runInTransaction(() -> {
            for (BluetoothDevice d : mChecked) {
                Device device = new Device();
                device.name = "四键开关";
                device.address = d.getAddress();
                device.timeAdded = new Date();
                Long[] ids = App.getInstance().getAppDatabase().deviceDao().insert(device);

                if (ids[0] == -1) {
                    return;
                }
                List<DeviceSwitchItem> items = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    DeviceSwitchItem item = new DeviceSwitchItem();
                    item.name = "开关" + i;
                    item.index = i;
                    item.deviceId = ids[0];
                    items.add(item);
                }
                DeviceSwitchItem[] arItems = new DeviceSwitchItem[items.size()];
                App.getInstance().getAppDatabase().deviceSwitchItemDao().insert(items.toArray(arItems));
            }
        });
    }
}

package com.winside.lighting.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@TargetApi(21)
class BeaconTransmitter {

    private Context mContext;
    private AdvertiseSettings mAdvertiseSettings;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private final List<AdvertiseCallback> mAdvertiseCallbacks;

    private boolean mCloseFlag;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int extraState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, Integer.MIN_VALUE);
                if (extraState != Integer.MIN_VALUE) {
                    switch (extraState) {
                        case BluetoothAdapter.STATE_OFF:
                            mBluetoothLeAdvertiser = null;
                            mAdvertiseSettings = null;
                            System.out.println("Bluetooth STATE_OFF");
                            return;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            System.out.println("Bluetooth STATE_TURNING_ON");
                            return;
                        case BluetoothAdapter.STATE_ON:
                            mBluetoothLeAdvertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
                            mAdvertiseSettings = new AdvertiseSettings.Builder()
                                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                                    .build();
                            System.out.println("Bluetooth STATE_ON");
                            return;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            System.out.println("Bluetooth STATE_TURNING_OFF");
                            return;
                        default:
                            return;
                    }
                }
                System.out.println("Bluetooth ERROR");
            }
        }
    };

    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            for (AdvertiseCallback c : mAdvertiseCallbacks) {
                c.onStartSuccess(settingsInEffect);
            }
        }

        @Override
        public void onStartFailure(int errorCode) {
            for (AdvertiseCallback c : mAdvertiseCallbacks) {
                c.onStartFailure(errorCode);
            }
        }
    };

    BeaconTransmitter(Context context) {
        this.mContext = context;
        this.mAdvertiseCallbacks = new CopyOnWriteArrayList<>();
        registerReceiver();
        if (BluetoothUtils.isBluetoothEnable()) {
            this.mBluetoothLeAdvertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
            this.mAdvertiseSettings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(false)
                    .build();
        } else {
            mCloseFlag = true;
            BluetoothUtils.openBlueAsyn();
        }
    }

    void close() {
        if (mCloseFlag) {
            BluetoothUtils.closeBlueAsyn();
            mCloseFlag = false;
        }
        unregisterReceiver();
    }

    void addAdvertiseCallback(AdvertiseCallback callback) {
        mAdvertiseCallbacks.add(callback);
    }

    void removeAdvertiseCallback(AdvertiseCallback callback) {
        mAdvertiseCallbacks.remove(callback);
    }

    void startAdvertising(byte[] data) {
        mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSettings, buildData(data), mAdvertiseCallback);
    }

    void stopAdvertising() {
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    private AdvertiseData buildData(byte[] data) {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(0x6977, data);
        return builder.build();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }
}

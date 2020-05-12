package com.winside.lighting.ble;

import android.bluetooth.BluetoothAdapter;

public class BluetoothUtils {

    private static final BluetoothAdapter BLUETOOTH_ADAPTER = BluetoothAdapter.getDefaultAdapter();

    /**
     * 蓝牙是否打开
     */
    public static boolean isBluetoothEnable() {
        return isSupportBluetooth() && BLUETOOTH_ADAPTER.isEnabled();
    }

    /**
     * 设备是否支持蓝牙
     */
    public static boolean isSupportBluetooth() {
        return BLUETOOTH_ADAPTER != null;
    }

    /**
     * 打开蓝牙
     */
    public static void openBlueAsyn() {
        if (isSupportBluetooth()) {
            BLUETOOTH_ADAPTER.enable();
        }
    }

    /**
     * 关闭蓝牙
     */
    public static void closeBlueAsyn() {
        if (isSupportBluetooth()) {
            BLUETOOTH_ADAPTER.disable();
        }
    }

}

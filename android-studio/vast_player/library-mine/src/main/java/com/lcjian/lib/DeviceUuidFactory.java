package com.lcjian.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import java.util.UUID;

@SuppressLint("HardwareIds")
public class DeviceUuidFactory {

    protected static final String PREFS_FILE = "device_id.xml";

    protected static final String PREFS_DEVICE_ID = "device_id";

    protected volatile static UUID uuid;

    private static String deviceID;

    public DeviceUuidFactory(Context context) {
        if (uuid == null) {
            synchronized (DeviceUuidFactory.class) {
                if (uuid == null) {
                    final SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        // Use the ids previously computed and stored in the prefs file
                        uuid = UUID.fromString(id);
                    } else {
                        uuid = UUID.nameUUIDFromBytes((deviceID + Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID)).getBytes());
                        // Write the value out to the prefs file
                        prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).apply();
                    }
                }
            }
        }
    }

    public UUID getDeviceUuid() {
        return uuid;
    }

    //静态代码块获取系统配置信息
    static {
        deviceID =
                Build.VERSION.CODENAME + ","
                        + Build.VERSION.INCREMENTAL + ","
                        + Build.VERSION.RELEASE + ","
                        + Build.VERSION.SDK + ","
                        + Build.VERSION.SDK_INT + ",";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (Build.SUPPORTED_32_BIT_ABIS.length != 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Build.SUPPORTED_32_BIT_ABIS.length; i++) {
                    sb.append(Build.SUPPORTED_32_BIT_ABIS[i]).append(",");
                }
                deviceID += sb;
            } else {
                deviceID += "unKnown,";
            }

            if (Build.SUPPORTED_64_BIT_ABIS.length != 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Build.SUPPORTED_64_BIT_ABIS.length; i++) {
                    sb.append(Build.SUPPORTED_64_BIT_ABIS[i]).append(",");
                }
                deviceID += sb;
            } else {
                deviceID += "unKnown,";
            }

            if (Build.SUPPORTED_ABIS.length != 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Build.SUPPORTED_ABIS.length; i++) {
                    sb.append(Build.SUPPORTED_ABIS[i]).append(",");
                }
                deviceID += sb;
            } else {
                deviceID += "unKnown,";
            }

        } else {
            deviceID += "unKnown,";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            deviceID += Build.VERSION.SECURITY_PATCH + ",";
            deviceID += Build.VERSION.BASE_OS + ",";
        } else {
            deviceID += "unKnown,";
        }
        deviceID
                += Build.TIME + ","
                + Build.SERIAL + ","
                + Build.getRadioVersion() + ","
                + Build.BOOTLOADER + ","
                + Build.FINGERPRINT + ","
                + Build.HARDWARE + ","
                + Build.BOARD + ","
                + Build.BRAND + ","
                + Build.CPU_ABI + ","
                + Build.CPU_ABI2 + ","
                + Build.DEVICE + ","
                + Build.HOST + ","
                + Build.ID + ","
                + Build.MANUFACTURER + ","
                + Build.MODEL + ","
                + Build.PRODUCT + ","
                + Build.TAGS + ","
                + Build.TYPE + ","
                + Build.USER + ","
                + Build.DISPLAY + ",";
    }
}
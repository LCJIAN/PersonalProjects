package com.winside.lighting.mesh;

import java.util.UUID;

public class WConstants {

    public static final UUID SERVICE_UUID = UUID.fromString("000018bb-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_WRITE_UUID = UUID.fromString("00002ADD-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_READ_UUID = UUID.fromString("00002ADE-0000-1000-8000-00805f9b34fb");

    public static class GattConfig {

        public static final byte[] OP_CODE = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x02};
        // DeviceKey
        public static final byte[] ATTR_TYPE_SET_DEVICE_KEY = new byte[]{(byte) 0x04, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_SET_DEVICE_KEY = new byte[]{(byte) 0x05, (byte) 0xF0};

        // 单播地址
        public static final byte[] ATTR_TYPE_SET_UNI_ADDRESS = new byte[]{(byte) 0x06, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_UNI_ADDRESS = new byte[]{(byte) 0x07, (byte) 0xF0};

        // IV
        public static final byte[] ATTR_TYPE_SET_IV = new byte[]{(byte) 0x08, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_IV = new byte[]{(byte) 0x09, (byte) 0xF0};

        // NetKey
        public static final byte[] ATTR_TYPE_SET_NET_KEY = new byte[]{(byte) 0x0A, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_NET_KEY = new byte[]{(byte) 0x0B, (byte) 0xF0};

        // AppKey
        public static final byte[] ATTR_TYPE_SET_APP_KEY = new byte[]{(byte) 0x0C, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_APP_KEY = new byte[]{(byte) 0x0D, (byte) 0xF0};

        // 配网组播地址
        public static final byte[] ATTR_TYPE_SET_GROUP_ADDRESS = new byte[]{(byte) 0x0E, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_GROUP_ADDRESS = new byte[]{(byte) 0x0F, (byte) 0xF0};

        // 重新订阅组播地址
        public static final byte[] ATTR_TYPE_RE_SUBSCRIBE_GROUP_ADDRESS = new byte[]{(byte) 0x10, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_RE_SUBSCRIBE_REPLY_GROUP_ADDRESS = new byte[]{(byte) 0x11, (byte) 0xF0};

        // 配置WIFI信息
        public static final byte[] ATTR_TYPE_SET_WIFI_INFO = new byte[]{(byte) 0x12, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_WIFI_INFO = new byte[]{(byte) 0x13, (byte) 0xF0};

        // 删除配置信息
        public static final byte[] ATTR_TYPE_DELETE_CONFIG = new byte[]{(byte) 0x14, (byte) 0xF0};
    }

    public static class Light {
        public static final byte[] OP_CODE_SWITCH = new byte[]{(byte) 0x00, (byte) 0x82, (byte) 0x02};
        public static final byte[] OP_CODE_SWITCH_REPLY = new byte[]{(byte) 0x00, (byte) 0x82, (byte) 0x04};
    }
}

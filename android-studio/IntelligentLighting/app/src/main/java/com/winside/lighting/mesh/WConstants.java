package com.winside.lighting.mesh;

public class WConstants {

    public static class GattConfig {

        // DeviceKey
        public static final byte[] OP_CODE_SET_DEVICE_KEY = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x02};

        public static final byte[] ATTR_TYPE_SET_DEVICE_KEY = new byte[]{(byte) 0x04, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_SET_DEVICE_KEY = new byte[]{(byte) 0x05, (byte) 0xF0};

        // 单播地址
        public static final byte[] OP_CODE_SET_UNICAST_ADDRESS = new byte[]{(byte) 0xCF, (byte) 0xA8, (byte) 0x01};

        public static final byte[] ATTR_TYPE_SET_UNICAST_ADDRESS = new byte[]{(byte) 0x06, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_UNICAST_ADDRESS = new byte[]{(byte) 0x07, (byte) 0xF0};

        // IV
        public static final byte[] OP_CODE_SET_IV = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x02};

        public static final byte[] ATTR_TYPE_SET_IV = new byte[]{(byte) 0x08, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_IV = new byte[]{(byte) 0x09, (byte) 0xF0};

        // NetKey
        public static final byte[] OP_CODE_SET_NET_KEY = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x02};

        public static final byte[] ATTR_TYPE_SET_NET_KEY = new byte[]{(byte) 0x0A, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_NET_KEY = new byte[]{(byte) 0x0B, (byte) 0xF0};

        // AppKey
        public static final byte[] OP_CODE_SET_APP_KEY = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x02};

        public static final byte[] ATTR_TYPE_SET_APP_KEY = new byte[]{(byte) 0x0C, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_APP_KEY = new byte[]{(byte) 0x0D, (byte) 0xF0};

        // 配网组播地址
        public static final byte[] OP_CODE_SET_GROUP_ADDRESS = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x02};

        public static final byte[] ATTR_TYPE_SET_GROUP_ADDRESS = new byte[]{(byte) 0x0E, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_GROUP_ADDRESS = new byte[]{(byte) 0x0F, (byte) 0xF0};

        // 重新订阅组播地址
        public static final byte[] OP_CODE_RE_SUBSCRIBE_GROUP_ADDRESS = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x02};

        public static final byte[] ATTR_TYPE_RE_SUBSCRIBE_GROUP_ADDRESS = new byte[]{(byte) 0x10, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_RE_SUBSCRIBE_REPLY_GROUP_ADDRESS = new byte[]{(byte) 0x11, (byte) 0xF0};

        // 配置WIFI信息
        public static final byte[] OP_CODE_SET_WIFI_INFO = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0x02};

        public static final byte[] ATTR_TYPE_SET_WIFI_INFO = new byte[]{(byte) 0x12, (byte) 0xF0};

        public static final byte[] ATTR_TYPE_REPLY_WIFI_INFO = new byte[]{(byte) 0x13, (byte) 0xF0};
    }
}

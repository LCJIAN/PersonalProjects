package com.winside.lighting.mesh;

import android.content.Context;
import android.os.AsyncTask;

import com.winside.lighting.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WTaskGattConfig extends AsyncTask<Void, Integer, Boolean> {

    private final String mMac;
    private final byte[] mDestAddress;
    private final byte[] mDeviceKey;
    private final byte[] mUniCast;
    private final byte[] mIvIndex;
    private final byte[] mIvState;
    private final byte[] mNetKeyIndex;
    private final byte[] mNetKey;
    private final byte[] mAppKeyIndex;
    private final byte[] mAppKey;
    private final List<byte[]> mGroupAddresses;
    private final byte[] mWifiSSID;
    private final byte[] mWifiPwd;
    private final BleClient mBleClient;

    public WTaskGattConfig(Context context,
                           String mac,
                           byte[] destAddress,
                           byte[] deviceKey,
                           byte[] uniCast,
                           byte[] ivIndex,
                           byte[] ivState,
                           byte[] netKeyIndex,
                           byte[] netKey,
                           byte[] appKeyIndex,
                           byte[] appKey,
                           List<byte[]> groupAddresses,
                           byte[] wifiSSID,
                           byte[] wifiPwd) {
        this.mMac = mac;
        this.mDestAddress = destAddress;
        this.mDeviceKey = deviceKey;
        this.mUniCast = uniCast;
        this.mIvIndex = ivIndex;
        this.mIvState = ivState;
        this.mNetKeyIndex = netKeyIndex;
        this.mNetKey = netKey;
        this.mAppKeyIndex = appKeyIndex;
        this.mAppKey = appKey;
        this.mGroupAddresses = groupAddresses;
        this.mWifiSSID = wifiSSID;
        this.mWifiPwd = wifiPwd;
        this.mBleClient = new BleClient(context, mMac, WConstants.SERVICE_UUID, WConstants.CHARACTERISTIC_WRITE_UUID,
                WConstants.CHARACTERISTIC_READ_UUID, new WEncoder(mMac), new WDecoder());
    }

    private boolean init() {
        if (!mBleClient.connect()) {
            return false;
        }
        if (!mBleClient.discoverServices()) {
            return false;
        }
        if (!mBleClient.enableNotification()) {
            return false;
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mBleClient.requestMtu();
    }

    private void destroy() {
        mBleClient.disconnect();
        mBleClient.release();
    }

    private boolean setDeviceKey() {
        List<byte[]> list = new ArrayList<>();
        final byte[] tid = new byte[]{WTidGenerator.getInstanceFor(mMac).get()};
        list.add(WConstants.GattConfig.OP_CODE);
        list.add(tid);
        list.add(WConstants.GattConfig.ATTR_TYPE_SET_DEVICE_KEY);
        list.add(mDeviceKey);
        byte[] message = Utils.concatForByte(list);
        assert message != null;

        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                byte[] m = WPacketUtils.getMessage(packet);
                return Arrays.equals(WPacketUtils.getSubBytes(m, 0, 3), WConstants.GattConfig.OP_CODE)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 3, 1), tid)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 4, 2), WConstants.GattConfig.ATTR_TYPE_REPLY_SET_DEVICE_KEY);
            }
        });
        mBleClient.addPacketCollector(packetCollector);

        if (mBleClient.sendData(WPacketUtils.buildPacket(mDestAddress, message))) {
            Packet packetResponse = null;
            try {
                packetResponse = packetCollector.nextResult(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBleClient.removePacketCollector(packetCollector);
            return packetResponse != null;
        } else {
            return false;
        }
    }

    private boolean setUniCast() {
        List<byte[]> list = new ArrayList<>();
        final byte[] tid = new byte[]{WTidGenerator.getInstanceFor(mMac).get()};
        list.add(WConstants.GattConfig.OP_CODE);
        list.add(tid);
        list.add(WConstants.GattConfig.ATTR_TYPE_SET_UNI_ADDRESS);
        list.add(new byte[]{0x01});
        list.add(mUniCast);
        byte[] message = Utils.concatForByte(list);
        assert message != null;

        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                byte[] m = WPacketUtils.getMessage(packet);
                return Arrays.equals(WPacketUtils.getSubBytes(m, 0, 3), WConstants.GattConfig.OP_CODE)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 3, 1), tid)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 4, 2), WConstants.GattConfig.ATTR_TYPE_REPLY_UNI_ADDRESS);
            }
        });
        mBleClient.addPacketCollector(packetCollector);

        if (mBleClient.sendData(WPacketUtils.buildPacket(mDestAddress, message))) {
            Packet packetResponse = null;
            try {
                packetResponse = packetCollector.nextResult(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBleClient.removePacketCollector(packetCollector);
            return packetResponse != null;
        } else {
            return false;
        }
    }

    private boolean setIV() {
        List<byte[]> list = new ArrayList<>();
        final byte[] tid = new byte[]{WTidGenerator.getInstanceFor(mMac).get()};
        list.add(WConstants.GattConfig.OP_CODE);
        list.add(tid);
        list.add(WConstants.GattConfig.ATTR_TYPE_SET_IV);
        list.add(mIvIndex);
        list.add(mIvState);
        byte[] message = Utils.concatForByte(list);
        assert message != null;

        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                byte[] m = WPacketUtils.getMessage(packet);
                return Arrays.equals(WPacketUtils.getSubBytes(m, 0, 3), WConstants.GattConfig.OP_CODE)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 3, 1), tid)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 4, 2), WConstants.GattConfig.ATTR_TYPE_REPLY_IV);
            }
        });
        mBleClient.addPacketCollector(packetCollector);

        if (mBleClient.sendData(WPacketUtils.buildPacket(mDestAddress, message))) {
            Packet packetResponse = null;
            try {
                packetResponse = packetCollector.nextResult(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBleClient.removePacketCollector(packetCollector);
            return packetResponse != null;
        } else {
            return false;
        }
    }

    private boolean setNetKey() {
        List<byte[]> list = new ArrayList<>();
        final byte[] tid = new byte[]{WTidGenerator.getInstanceFor(mMac).get()};
        list.add(WConstants.GattConfig.OP_CODE);
        list.add(tid);
        list.add(WConstants.GattConfig.ATTR_TYPE_SET_NET_KEY);
        list.add(mNetKeyIndex);
        list.add(mNetKey);
        byte[] message = Utils.concatForByte(list);
        assert message != null;

        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                byte[] m = WPacketUtils.getMessage(packet);
                return Arrays.equals(WPacketUtils.getSubBytes(m, 0, 3), WConstants.GattConfig.OP_CODE)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 3, 1), tid)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 4, 2), WConstants.GattConfig.ATTR_TYPE_REPLY_NET_KEY);
            }
        });
        mBleClient.addPacketCollector(packetCollector);

        if (mBleClient.sendData(WPacketUtils.buildPacket(mDestAddress, message))) {
            Packet packetResponse = null;
            try {
                packetResponse = packetCollector.nextResult(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBleClient.removePacketCollector(packetCollector);
            return packetResponse != null;
        } else {
            return false;
        }
    }

    private boolean setAppKey() {
        List<byte[]> list = new ArrayList<>();
        final byte[] tid = new byte[]{WTidGenerator.getInstanceFor(mMac).get()};
        list.add(WConstants.GattConfig.OP_CODE);
        list.add(tid);
        list.add(WConstants.GattConfig.ATTR_TYPE_SET_APP_KEY);
        list.add(mAppKeyIndex);
        list.add(mAppKey);
        byte[] message = Utils.concatForByte(list);
        assert message != null;

        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                byte[] m = WPacketUtils.getMessage(packet);
                return Arrays.equals(WPacketUtils.getSubBytes(m, 0, 3), WConstants.GattConfig.OP_CODE)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 3, 1), tid)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 4, 2), WConstants.GattConfig.ATTR_TYPE_REPLY_APP_KEY);
            }
        });
        mBleClient.addPacketCollector(packetCollector);

        if (mBleClient.sendData(WPacketUtils.buildPacket(mDestAddress, message))) {
            Packet packetResponse = null;
            try {
                packetResponse = packetCollector.nextResult(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBleClient.removePacketCollector(packetCollector);
            return packetResponse != null;
        } else {
            return false;
        }
    }

    private boolean setGroupAddress() {
        List<byte[]> list = new ArrayList<>();
        final byte[] tid = new byte[]{WTidGenerator.getInstanceFor(mMac).get()};
        list.add(WConstants.GattConfig.OP_CODE);
        list.add(tid);
        list.add(WConstants.GattConfig.ATTR_TYPE_SET_GROUP_ADDRESS);
        list.add(new byte[]{(byte) mGroupAddresses.size()});
        list.addAll(mGroupAddresses);
        byte[] message = Utils.concatForByte(list);
        assert message != null;

        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                byte[] m = WPacketUtils.getMessage(packet);
                return Arrays.equals(WPacketUtils.getSubBytes(m, 0, 3), WConstants.GattConfig.OP_CODE)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 3, 1), tid)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 4, 2), WConstants.GattConfig.ATTR_TYPE_REPLY_GROUP_ADDRESS);
            }
        });
        mBleClient.addPacketCollector(packetCollector);

        if (mBleClient.sendData(WPacketUtils.buildPacket(mDestAddress, message))) {
            Packet packetResponse = null;
            try {
                packetResponse = packetCollector.nextResult(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBleClient.removePacketCollector(packetCollector);
            return packetResponse != null;
        } else {
            return false;
        }
    }

    private boolean reSubscribeGroupAddress() {
        List<byte[]> list = new ArrayList<>();
        final byte[] tid = new byte[]{WTidGenerator.getInstanceFor(mMac).get()};
        list.add(WConstants.GattConfig.OP_CODE);
        list.add(tid);
        list.add(WConstants.GattConfig.ATTR_TYPE_RE_SUBSCRIBE_GROUP_ADDRESS);
        list.add(new byte[]{(byte) mGroupAddresses.size()});
        list.addAll(mGroupAddresses);
        byte[] message = Utils.concatForByte(list);
        assert message != null;

        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                byte[] m = WPacketUtils.getMessage(packet);
                return Arrays.equals(WPacketUtils.getSubBytes(m, 0, 3), WConstants.GattConfig.OP_CODE)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 3, 1), tid)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 4, 2), WConstants.GattConfig.ATTR_TYPE_RE_SUBSCRIBE_REPLY_GROUP_ADDRESS);
            }
        });
        mBleClient.addPacketCollector(packetCollector);

        if (mBleClient.sendData(WPacketUtils.buildPacket(mDestAddress, message))) {
            Packet packetResponse = null;
            try {
                packetResponse = packetCollector.nextResult(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBleClient.removePacketCollector(packetCollector);
            return packetResponse != null;
        } else {
            return false;
        }
    }

    private boolean setWifiInfo() {
        List<byte[]> list = new ArrayList<>();
        final byte[] tid = new byte[]{WTidGenerator.getInstanceFor(mMac).get()};
        list.add(WConstants.GattConfig.OP_CODE);
        list.add(tid);
        list.add(WConstants.GattConfig.ATTR_TYPE_SET_WIFI_INFO);
        list.add(new byte[]{(byte) mWifiSSID.length});
        list.add(mWifiSSID);
        list.add(new byte[]{(byte) mWifiPwd.length});
        list.add(mWifiPwd);
        byte[] message = Utils.concatForByte(list);
        assert message != null;

        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                byte[] m = WPacketUtils.getMessage(packet);
                return Arrays.equals(WPacketUtils.getSubBytes(m, 0, 3), WConstants.GattConfig.OP_CODE)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 3, 1), tid)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 4, 2), WConstants.GattConfig.ATTR_TYPE_REPLY_WIFI_INFO);
            }
        });
        mBleClient.addPacketCollector(packetCollector);

        if (mBleClient.sendData(WPacketUtils.buildPacket(mDestAddress, message))) {
            Packet packetResponse = null;
            try {
                packetResponse = packetCollector.nextResult(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBleClient.removePacketCollector(packetCollector);
            return packetResponse != null;
        } else {
            return false;
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (init()) {
            publishProgress(10);
        } else {
            destroy();
            return false;
        }
        if (setDeviceKey()) {
            publishProgress(20);
        } else {
            destroy();
            return false;
        }
        if (setUniCast()) {
            publishProgress(30);
        } else {
            destroy();
            return false;
        }
        if (setIV()) {
            publishProgress(40);
        } else {
            destroy();
            return false;
        }
        if (setNetKey()) {
            publishProgress(50);
        } else {
            destroy();
            return false;
        }
        if (setAppKey()) {
            publishProgress(60);
        } else {
            destroy();
            return false;
        }
        if (setGroupAddress()) {
            publishProgress(70);
        } else {
            destroy();
            return false;
        }
//        if (reSubscribeGroupAddress()) {
//            publishProgress(80);
//        } else {
//            destroy();
//            return false;
//        }
        if (mWifiSSID != null) {
            if (setWifiInfo()) {
                publishProgress(90);
            } else {
                destroy();
                return false;
            }
        }
        destroy();
        return true;
    }
}

package com.winside.lighting.ble;

import android.content.Context;

import com.winside.lighting.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeaconClient {

    private static final byte[] LOCAL_NAME_PREFIX = new byte[]{(byte) 0x77, (byte) 0x69};
    private static final byte[] LOCAL_NAME_SUFFIX = new byte[]{(byte) 0x6E, (byte) 0x73, (byte) 0x69, (byte) 0x64};
    private static final byte[] VERSION = new byte[]{(byte) 0x00, (byte) 0x01};
    private static final byte[] MESSAGE_HOLDER;

    static {
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            list.add(new byte[]{(byte) 0xFF});
        }
        MESSAGE_HOLDER = Utils.concatForByte(list);
    }

    private static byte tid = 1;
    private BeaconTransmitter mBeaconTransmitter;

    public BeaconClient(Context context) {
        this.mBeaconTransmitter = new BeaconTransmitter(context);
    }

    public void close() {
        mBeaconTransmitter.close();
    }

    public void joinInTheNetwork() {
        mBeaconTransmitter.stopAdvertising();
        byte[] message = Arrays.copyOf(MESSAGE_HOLDER, MESSAGE_HOLDER.length);
        message[0] = 0x00;
        message[1] = 0x10;
        mBeaconTransmitter.startAdvertising(buildData(message));
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBeaconTransmitter.stopAdvertising();
    }

    public void leaveFromTheNetwork() {
        mBeaconTransmitter.stopAdvertising();
        byte[] message = Arrays.copyOf(MESSAGE_HOLDER, MESSAGE_HOLDER.length);
        message[0] = 0x01;
        message[1] = 0x10;
        mBeaconTransmitter.startAdvertising(buildData(message));
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBeaconTransmitter.stopAdvertising();
    }

    public void switchOnOff(boolean on) {
        mBeaconTransmitter.stopAdvertising();
        byte[] message = Arrays.copyOf(MESSAGE_HOLDER, MESSAGE_HOLDER.length);
        message[0] = 0x02;
        message[1] = 0x10;
        message[2] = (byte) (on ? 0x01 : 0x00);
        mBeaconTransmitter.startAdvertising(buildData(message));
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBeaconTransmitter.stopAdvertising();
    }

    public void pause() {
        mBeaconTransmitter.stopAdvertising();
        byte[] message = Arrays.copyOf(MESSAGE_HOLDER, MESSAGE_HOLDER.length);
        message[0] = 0x02;
        message[1] = 0x10;
        message[2] = 0x02;
        mBeaconTransmitter.startAdvertising(buildData(message));
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBeaconTransmitter.stopAdvertising();
    }

    private byte[] buildData(byte[] message) {
        final byte[] tid = new byte[]{getTidAndIncrement()};
        byte[] bytes = Utils.concatForByte(Arrays.asList(LOCAL_NAME_PREFIX, LOCAL_NAME_SUFFIX, VERSION, tid, message));
        assert bytes != null;
        byte crc = calculateCRC(bytes);
        return Utils.concatForByte(Arrays.asList(LOCAL_NAME_SUFFIX, VERSION, tid, message, new byte[]{crc}));
    }

    private byte getTidAndIncrement() {
        byte result = tid;
        tid = (byte) (tid + 1);
        return result;
    }

    private static byte calculateCRC(byte[] bytes) {
        byte crc = 0x00;
        for (byte b : bytes) {
            crc ^= b;
            for (int x = 8; x > 0; --x) { /* 下面这段计算过程与计算一个字节crc一样 */
                if ((crc & 0x80) != 0) {
                    crc = (byte) ((crc << 1) ^ 0x07); // 多项式 0x07
                } else {
                    crc = (byte) (crc << 1);
                }
            }
        }
        return crc;
    }
}

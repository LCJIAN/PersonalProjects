package com.winside.lighting.mesh;

import android.content.Context;

import java.util.UUID;

public class WGattConfigTask {

    private final String mMac;
    private final BleClient mBleClient;

    public WGattConfigTask(Context context, UUID serviceUUID, String mac) {
        this.mMac = mac;
        this.mBleClient = new BleClient(context, serviceUUID, new WEncoder(), new WDecoder());
    }

    private void init() {
        mBleClient.connect(mMac);
    }

    private void destroy() {
        mBleClient.disconnect();
        mBleClient.release();
    }

    private boolean setDeviceKey() {
        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                return false;
            }
        });
        mBleClient.addPacketCollector(packetCollector);

        mBleClient.sendData(WPacketUtils.buildPacket());

        Packet packetResponse = null;
        try {
            packetResponse = packetCollector.nextResult(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBleClient.removePacketCollector(packetCollector);
        return packetResponse == null;
    }
}

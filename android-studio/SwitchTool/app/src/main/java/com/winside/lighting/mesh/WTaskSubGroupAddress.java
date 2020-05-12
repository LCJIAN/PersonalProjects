package com.winside.lighting.mesh;

import android.content.Context;
import android.os.AsyncTask;

import com.winside.lighting.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WTaskSubGroupAddress extends AsyncTask<Void, Integer, Boolean> {

    private final String mMac;
    private final byte[] mDestAddress;
    private final List<Byte> mIndexes;
    private final List<byte[]> mGroupAddresses;
    private final BleClient mBleClient;

    public WTaskSubGroupAddress(Context context, String mac, byte[] destAddress, List<Byte> indexes, List<byte[]> groupAddresses) {
        this.mMac = mac;
        this.mDestAddress = destAddress;
        this.mIndexes = indexes;
        this.mGroupAddresses = groupAddresses;
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

    private boolean subGroupAddress(int i) {
        List<byte[]> list = new ArrayList<>();
        final byte[] tid = new byte[]{WTidGenerator.getInstanceFor(mMac).get()};
        list.add(WConstants.GattConfig.OP_CODE);
        list.add(tid);
        list.add(WConstants.GattConfig.ATTR_TYPE_GROUP_ADDRESS_SUB);
        list.add(new byte[]{mIndexes.get(i)});
        list.add(mGroupAddresses.get(i));
        byte[] message = Utils.concatForByte(list);
        assert message != null;

        PacketCollector packetCollector = new PacketCollector(new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                byte[] m = WPacketUtils.getMessage(packet);
                return Arrays.equals(WPacketUtils.getSubBytes(m, 0, 3), WConstants.GattConfig.OP_CODE)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 3, 1), tid)
                        && Arrays.equals(WPacketUtils.getSubBytes(m, 4, 2), WConstants.GattConfig.ATTR_TYPE_GROUP_ADDRESS_SUB_REPLY);
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
            publishProgress(50);
        } else {
            destroy();
            return false;
        }
        for (int i = 0; i < mIndexes.size(); i++) {
            if (subGroupAddress(i)) {
                publishProgress(50 + i);
            } else {
                destroy();
                return false;
            }
        }
        publishProgress(100);
        destroy();
        return true;
    }

    @Override
    protected void onCancelled() {
        destroy();
    }
}

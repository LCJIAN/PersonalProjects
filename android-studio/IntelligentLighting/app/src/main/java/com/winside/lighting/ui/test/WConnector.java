package com.winside.lighting.ui.test;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.winside.lighting.mesh.BleClient;
import com.winside.lighting.mesh.WConstants;
import com.winside.lighting.mesh.WDecoder;
import com.winside.lighting.mesh.WEncoder;
import com.winside.lighting.mesh.WPacketUtils;
import com.winside.lighting.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class WConnector {

    private Listener mListener;

    private Context mContext;
    private String mMac;

    public WConnector(Context context, String mac) {
        this.mContext = context;
        this.mMac = mac;
    }

    public void connect() {
        new Thread(() -> {
            mListener.onStartConnect();
            BleClient bleClient = new BleClient(mContext, mMac, WConstants.SERVICE_UUID,
                    WConstants.CHARACTERISTIC_WRITE_UUID, WConstants.CHARACTERISTIC_READ_UUID, new WEncoder(mMac), new WDecoder());
            bleClient.addGattCallback(new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        mListener.onDisconnected();
                    }
                }
            });
            if (!bleClient.connect()) {
                bleClient.disconnect();
                bleClient.release();
                mListener.onDisconnected();
                return;
            } else {
                mListener.onConnected();
            }
            mListener.onStartDiscoverServices();
            if (!bleClient.discoverServices()) {
                bleClient.disconnect();
                bleClient.release();
                mListener.onDiscoverServicesFailed();
                return;
            } else {
                mListener.onDiscoverServicesSuccess();
            }
            while (true) {
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mListener.onSendData();

                {
                    List<byte[]> list = new ArrayList<>();
                    list.add(WConstants.Light.OP_CODE_SWITCH);
                    list.add(new byte[]{(byte) 0x01});
                    byte[] message = Utils.concatForByte(list);
                    assert message != null;
                    boolean result = bleClient.sendData(WPacketUtils.buildPacket(new byte[]{0x00, 0x00}, message));
                    if (!result) {
                        bleClient.disconnect();
                        bleClient.release();
                        break;
                    }
                }
            }
        }).start();
    }

    public WConnector setListener(Listener listener) {
        this.mListener = listener;
        return this;
    }

    public interface Listener {

        void onStartConnect();

        void onConnected();

        void onDisconnected();

        void onStartDiscoverServices();

        void onDiscoverServicesSuccess();

        void onDiscoverServicesFailed();

        void onSendData();

    }
}

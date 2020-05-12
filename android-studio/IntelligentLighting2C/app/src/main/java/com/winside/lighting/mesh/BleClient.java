package com.winside.lighting.mesh;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class BleClient {

    private BluetoothGatt mBluetoothGatt;

    // 蓝牙状态
    private int mState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 1;
    private static final int STATE_SERVICE_FAIL = 2;
    private static final int STATE_SERVICE_SUCCESS = 3;
    private static final int STATE_MTU_SET_FAIL = 4;
    private static final int STATE_MTU_SET_SUCCESS = 5;

    private int mMtu = 115;

    private String mMac;
    // 服务标识
    private UUID SERVICE_UUID;
    // 特征标识（读取数据）
    private UUID CHARACTERISTIC_READ_UUID;
    // 特征标识（发送数据）
    private UUID CHARACTERISTIC_WRITE_UUID;
    // 特征标识（notify）
    private UUID CHARACTERISTIC_NOTIFY_UUID;
    // 特征标识（indicate）
    private UUID CHARACTERISTIC_INDICATE_UUID;
    // 描述标识
    private UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final Context mContext;
    private final BluetoothAdapter mBluetoothAdapter;
    private final List<BluetoothGattCallback> mGattCallbacks;

    private final Encoder mEncoder;
    private final Decoder mDecoder;
    private final List<PacketCollector> mCollectors;
    private final List<PacketListener> mListeners;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                for (BluetoothGattCallback c : mGattCallbacks) {
                    c.onPhyUpdate(gatt, txPhy, rxPhy, status);
                }
            }
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                for (BluetoothGattCallback c : mGattCallbacks) {
                    c.onPhyRead(gatt, txPhy, rxPhy, status);
                }
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onConnectionStateChange(gatt, status, newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onServicesDiscovered(gatt, status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onCharacteristicRead(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onCharacteristicWrite(gatt, characteristic, status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (CHARACTERISTIC_READ_UUID.equals(characteristic.getUuid())) {
                handleValue(characteristic.getValue());
            }

            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onCharacteristicChanged(gatt, characteristic);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onDescriptorRead(gatt, descriptor, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onDescriptorWrite(gatt, descriptor, status);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onReliableWriteCompleted(gatt, status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onReadRemoteRssi(gatt, rssi, status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                for (BluetoothGattCallback c : mGattCallbacks) {
                    c.onMtuChanged(gatt, mtu, status);
                }
            }
        }
    };

    public BleClient(Context context, String mac, UUID serviceUUID, UUID characteristicWriteUUID,
                     UUID characteristicReadUUID, Encoder encoder, Decoder decoder) {
        this.mContext = context;
        this.mMac = mac;
        this.SERVICE_UUID = serviceUUID;
        this.CHARACTERISTIC_WRITE_UUID = characteristicWriteUUID;
        this.CHARACTERISTIC_READ_UUID = characteristicReadUUID;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mGattCallbacks = new CopyOnWriteArrayList<>();

        this.mEncoder = encoder;
        this.mDecoder = decoder;
        this.mCollectors = new CopyOnWriteArrayList<>();
        this.mListeners = new CopyOnWriteArrayList<>();
    }

    public void addGattCallback(BluetoothGattCallback callback) {
        mGattCallbacks.add(callback);
    }

    public void removeLGattCallback(BluetoothGattCallback callback) {
        mGattCallbacks.remove(callback);
    }

    public void addPacketCollector(PacketCollector collector) {
        mCollectors.add(collector);
    }

    public void removePacketCollector(PacketCollector collector) {
        mCollectors.remove(collector);
    }

    public void addPacketListener(PacketListener listener) {
        mListeners.add(listener);
    }

    public void removePacketListener(PacketListener listener) {
        mListeners.remove(listener);
    }

    public boolean connect() {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mMac);
        if (device == null) {
            return false;
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        BluetoothGattCallback callback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mState = STATE_CONNECTED;
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mState = STATE_DISCONNECTED;
                }
                countDownLatch.countDown();
            }
        };
        mGattCallbacks.add(callback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mGattCallbacks.remove(callback);

        return mState == STATE_CONNECTED;
    }

    public boolean discoverServices() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        BluetoothGattCallback callback = new BluetoothGattCallback() {

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // 获取蓝牙设备的服务
                    BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);
                    if (gattService == null) {
                        mState = STATE_SERVICE_FAIL;
                    } else {
                        if (CHARACTERISTIC_READ_UUID == null) {
                            findUUIDs();
                        }
                        mState = STATE_SERVICE_SUCCESS;
                    }
                } else {
                    mState = STATE_SERVICE_FAIL;
                }
                countDownLatch.countDown();
            }
        };
        mGattCallbacks.add(callback);
        mBluetoothGatt.discoverServices();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mGattCallbacks.remove(callback);

        return mState == STATE_SERVICE_SUCCESS;
    }

    public boolean enableNotification() {
        BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);
        // 获取蓝牙设备的特征
        BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_READ_UUID);
        if (gattCharacteristic == null) {
            return false;
        } else {
            // 获取蓝牙设备特征的描述符
            BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            if (!mBluetoothGatt.writeDescriptor(descriptor)) {
                return false;
            } else {
                // 蓝牙设备在数据改变时，通知App，App在收到数据后回调onCharacteristicChanged方法
                return mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
            }
        }
    }

    public boolean requestMtu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            BluetoothGattCallback callback = new BluetoothGattCallback() {
                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        mState = STATE_MTU_SET_SUCCESS;
                    } else {
                        mState = STATE_MTU_SET_FAIL;
                    }
                    mMtu = mtu;
                    countDownLatch.countDown();
                }
            };
            mGattCallbacks.add(callback);
            mBluetoothGatt.requestMtu(mMtu);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mGattCallbacks.remove(callback);

            return mState == STATE_MTU_SET_SUCCESS;
        }

        return false;
    }

    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void release() {
        if (mBluetoothGatt == null) {
            return;
        }
        refreshGattCache(mBluetoothGatt);
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public boolean sendData(Packet packet) {
        List<byte[]> a = mEncoder.encode(packet, mMtu);
        for (byte[] b : a) {
            // 获取蓝牙设备的服务
            BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);
            // 获取蓝牙设备的特征
            BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_WRITE_UUID);
            // 发送数据
            gattCharacteristic.setValue(b);
            if (!mBluetoothGatt.writeCharacteristic(gattCharacteristic)) {
                return false;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean sendData(byte[] data) {
        BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);
        // 获取蓝牙设备的特征
        BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_WRITE_UUID);
        // 发送数据
        gattCharacteristic.setValue(data);
        return mBluetoothGatt.writeCharacteristic(gattCharacteristic);
    }

    public boolean readData() {
        // 获取蓝牙设备的服务
        BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);
        // 获取蓝牙设备的特征
        BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_READ_UUID);
        // 发送数据
        return mBluetoothGatt.readCharacteristic(gattCharacteristic);
    }

    private void findUUIDs() {
        List<BluetoothGattService> bluetoothGattServices = mBluetoothGatt.getServices();
        for (BluetoothGattService bluetoothGattService : bluetoothGattServices) {
            if (bluetoothGattService.getUuid().equals(SERVICE_UUID)) {
                List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    int charaProp = characteristic.getProperties();
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        CHARACTERISTIC_READ_UUID = characteristic.getUuid();
                    }
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                        CHARACTERISTIC_WRITE_UUID = characteristic.getUuid();
                    }
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                        CHARACTERISTIC_WRITE_UUID = characteristic.getUuid();
                    }
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        CHARACTERISTIC_NOTIFY_UUID = characteristic.getUuid();
                    }
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                        CHARACTERISTIC_INDICATE_UUID = characteristic.getUuid();
                    }
                }
            }
        }
    }

    private void handleValue(byte[] value) {
        Packet packet = mDecoder.decode(value);
        if (packet != null) {
            for (PacketListener listener : mListeners) {
                listener.processPacket(packet);
            }
            for (PacketCollector collector : mCollectors) {
                collector.processPacket(packet);
            }
        }
    }

    private static void refreshGattCache(BluetoothGatt gatt) {
        try {
            if (gatt != null) {
                Method refresh = BluetoothGatt.class.getMethod("refresh");
                refresh.setAccessible(true);
                refresh.invoke(gatt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

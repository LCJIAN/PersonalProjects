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
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BleClient {

    private BluetoothGatt mBluetoothGatt;

    private int mtu;
    // 蓝牙连接状态
    private int mConnectionState = 0;
    /**
     * The profile is in disconnected state
     */
    private static final int STATE_DISCONNECTED = 0;
    /**
     * The profile is in connecting state
     */
    private static final int STATE_CONNECTING = 1;
    /**
     * The profile is in connected state
     */
    private static final int STATE_CONNECTED = 2;
    /**
     * The profile is in disconnecting state
     */
    private static final int STATE_DISCONNECTING = 3;
    /**
     * The profile is in disconnecting state
     */
    private static final int STATE_SERVICE_FAIL = 4;
    /**
     * The profile is in disconnecting state
     */
    private static final int STATE_SERVICE_SUCCESS = 5;

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

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    c.onPhyUpdate(gatt, txPhy, rxPhy, status);
                }
            }
        }

        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    c.onPhyRead(gatt, txPhy, rxPhy, status);
                }
            }
        }

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                release();
            }

            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onConnectionStateChange(gatt, status, newState);
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 获取蓝牙设备的服务
                BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);
                if (gattService == null) {
                    mConnectionState = STATE_SERVICE_FAIL;
                    disconnect();
                    release();
                } else {
                    findUUIDs();

                    // 获取蓝牙设备的特征
                    BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_READ_UUID);
                    if (gattCharacteristic == null) {
                        mConnectionState = STATE_SERVICE_FAIL;
                        disconnect();
                        release();
                    } else {
                        mConnectionState = STATE_SERVICE_SUCCESS;
                        // 获取蓝牙设备特征的描述符
                        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(DESCRIPTOR_UUID);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        if (mBluetoothGatt.writeDescriptor(descriptor)) {
                            // 蓝牙设备在数据改变时，通知App，App在收到数据后回调onCharacteristicChanged方法
                            mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
                        }
                    }
                }

            }

            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onServicesDiscovered(gatt, status);
            }
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onCharacteristicRead(gatt, characteristic, status);
            }
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onCharacteristicWrite(gatt, characteristic, status);
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (CHARACTERISTIC_READ_UUID.equals(characteristic.getUuid())) {
                handleValue(characteristic.getValue());
            }

            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onCharacteristicChanged(gatt, characteristic);
            }
        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onDescriptorRead(gatt, descriptor, status);
            }
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onDescriptorWrite(gatt, descriptor, status);
            }
        }

        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onReliableWriteCompleted(gatt, status);
            }
        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                c.onReadRemoteRssi(gatt, rssi, status);
            }
        }

        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            for (BluetoothGattCallback c : mGattCallbacks) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    c.onMtuChanged(gatt, mtu, status);
                }
            }
        }
    };

    public BleClient(Context context, UUID serviceUUID, Encoder encoder, Decoder decoder) {
        this.mContext = context;
        this.SERVICE_UUID = serviceUUID;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mGattCallbacks = new CopyOnWriteArrayList<>();

        this.mEncoder = encoder;
        this.mDecoder = decoder;
        this.mCollectors = new CopyOnWriteArrayList<>();
        this.mListeners = new CopyOnWriteArrayList<>();
    }

    public boolean hasBleFeature() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 蓝牙是否打开
     */
    public boolean isBluetoothEnable() {
        return isSupportBluetooth() && mBluetoothAdapter.isEnabled();
    }

    /**
     * 设备是否支持蓝牙
     */
    public boolean isSupportBluetooth() {
        return mBluetoothAdapter != null;
    }

    public void openBlueAsyn() {
        if (isSupportBluetooth()) {
            mBluetoothAdapter.enable();
        }
    }

    public void closeBlueAsyn() {
        if (isSupportBluetooth()) {
            mBluetoothAdapter.disable();
        }
    }

    public void addGattCallback(BluetoothGattCallback callback) {
        mGattCallbacks.add(callback);
    }

    public void removeLGattCallback(BluetoothGattCallback callback) {
        mGattCallbacks.remove(callback);
    }

    public void connect(String address) {
        if (mConnectionState == STATE_DISCONNECTED) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            if (device == null) {
                return;
            }
            mConnectionState = STATE_CONNECTING;
            mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        }
    }

    public void disconnect() {
        if (mConnectionState == STATE_CONNECTED
                || mConnectionState == STATE_SERVICE_SUCCESS
                || mConnectionState == STATE_SERVICE_FAIL) {
            if (mBluetoothGatt == null) {
                return;
            }
            mConnectionState = STATE_DISCONNECTING;
            mBluetoothGatt.disconnect();
        }
    }

    public void release() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public boolean sendData(Packet packet) {
        List<byte[]> a = mEncoder.encode(packet, mtu);
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
        }
        return true;
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

    private final Encoder mEncoder;
    private final Decoder mDecoder;
    private final List<PacketCollector> mCollectors;
    private final List<PacketListener> mListeners;

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
}

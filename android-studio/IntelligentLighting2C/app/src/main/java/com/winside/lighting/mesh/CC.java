package com.winside.lighting.mesh;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class CC {

    private final Context mContext;
    private final List<String> mMacs;

    private final Lock mLock;
    private final Condition mConditionStart;
    private final Condition mConditionConnect;
    private final Condition mConditionTask;

    private final BluetoothLeScannerCompat mScanner;

    private final BlockingDeque<Packet> mTasksQueue;

    private final AtomicBoolean mStopFlag;
    private ScanResult mScanResult = null;
    private BleClient mBleClient = null;
    private int mNext = 1;

    public CC(Context context) {
        this.mContext = context;
        this.mMacs = new ArrayList<>();
        this.mLock = new ReentrantLock();
        this.mConditionStart = mLock.newCondition();
        this.mConditionConnect = mLock.newCondition();
        this.mConditionTask = mLock.newCondition();
        this.mScanner = BluetoothLeScannerCompat.getScanner();
        this.mTasksQueue = new LinkedBlockingDeque<>();
        this.mStopFlag = new AtomicBoolean();
    }

    public void restart(List<String> macs) {
        new Thread(() -> {
            stop();
            try {
                mLock.lock();
                while (mNext != 1) {
                    mConditionStart.await();
                }
                if (macs != null && !macs.isEmpty()) {
                    mMacs.clear();
                    mMacs.addAll(macs);
                }
                mStopFlag.getAndSet(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mLock.unlock();
            }
            start();
        }).start();
    }

    public void enqueue(Packet packet) {
        try {
            mTasksQueue.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        new Thread(this::scan).start();
        new Thread(this::connect).start();
        new Thread(this::startTask).start();
    }

    private void stop() {
        mStopFlag.getAndSet(true);
    }

    private void scan() {
        try {
            mLock.lock();

            mScanResult = null;
            while (mScanResult == null && !mStopFlag.get()) {
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                ScanCallback scanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, @NonNull ScanResult result) {
                        if (mMacs.contains(result.getDevice().getAddress())) {
                            if (mScanResult == null || mScanResult.getRssi() < result.getRssi()) {
                                mScanResult = result;
                            }
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        countDownLatch.countDown();
                    }
                };

                mScanner.startScan(
                        null,
                        new ScanSettings.Builder()
                                .setLegacy(false)
                                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                                .setUseHardwareBatchingIfSupported(false)
                                .build(),
                        scanCallback);

                new Thread(() -> {
                    try {
                        Thread.sleep(5 * 1000);
                        countDownLatch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mScanner.stopScan(scanCallback);
            }
            mNext = 2;
            mConditionConnect.signalAll();
        } finally {
            mLock.unlock();
        }
    }

    private void connect() {
        try {
            mLock.lock();

            while (mNext != 2) {
                mConditionConnect.await();
            }
            if (mScanResult == null) {
                return;
            }

            String mac = mScanResult.getDevice().getAddress();
            while (mBleClient == null && !mStopFlag.get()) {
                BleClient bleClient = new BleClient(mContext, mac, WConstants.SERVICE_UUID, WConstants.CHARACTERISTIC_WRITE_UUID,
                        WConstants.CHARACTERISTIC_READ_UUID, new WEncoder(mac), new WDecoder());
                if (bleClient.connect()
                        && bleClient.discoverServices()
                        && bleClient.enableNotification()
                        && bleClient.requestMtu()) {
                    mBleClient = bleClient;
                    mBleClient.addGattCallback(new BluetoothGattCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                restart(null);
                            }
                        }
                    });
                } else {
                    bleClient.disconnect();
                    bleClient.release();
                }
            }
            mNext = 3;
            mConditionTask.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mLock.unlock();
        }
    }

    private void startTask() {
        try {
            mLock.lock();

            while (mNext != 3) {
                mConditionTask.await();
            }
            if (mBleClient != null) {
                while (!mStopFlag.get()) {
                    try {
                        Packet packet = mTasksQueue.take();
                        mBleClient.sendData(packet);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mBleClient.disconnect();
                mBleClient.release();
            }
            mBleClient = null;
            mScanResult = null;
            mNext = 1;
            mConditionStart.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mLock.unlock();
        }
    }
}

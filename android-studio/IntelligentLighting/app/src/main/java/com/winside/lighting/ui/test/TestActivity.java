package com.winside.lighting.ui.test;

import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winside.lighting.R;
import com.winside.lighting.mesh.BleClient;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.util.DateUtils;
import com.winside.lighting.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class TestActivity extends BaseActivity {

    @BindView(R.id.btn_test)
    Button btn_test;
    @BindView(R.id.tv_log)
    TextView tv_log;

    private static final String FORMAT = "HH:mm:ss SSS";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

//        btn_test.setOnClickListener(v -> new ScanTask("C3:5F:61:2B:6B:9D").execute());
        btn_test.setOnClickListener(v -> new ConnectTask("EA:39:D6:3A:B0:07", UUID.fromString("000018bb-0000-1000-8000-00805f9b34fb")).execute());
    }

    private void appendLog(String log) {
        tv_log.post(() -> tv_log.append(log));
    }

    private class ScanTask extends AsyncTask<Void, Integer, BluetoothDevice> {

        private String mMac;

        private BluetoothDevice mDevice;

        ScanTask(String mMac) {
            this.mMac = mMac;
        }

        @Override
        protected void onPreExecute() {
            showProgress();
        }

        @Override
        protected BluetoothDevice doInBackground(Void... voids) {

            CountDownLatch countDownLatch = new CountDownLatch(1);

            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            ScanSettings settings = new ScanSettings.Builder()
                    .setLegacy(false)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            List<ScanFilter> filters = new ArrayList<>();
            filters.add(new ScanFilter.Builder().setDeviceAddress(mMac).build());
            ScanCallback scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, @NonNull ScanResult result) {
                    appendLog("onScanResult" + DateUtils.convertDateToStr(DateUtils.now(), FORMAT) + "\n");
                    mDevice = result.getDevice();
                    countDownLatch.countDown();
                    scanner.stopScan(this);
                }

                @Override
                public void onBatchScanResults(@NonNull List<ScanResult> results) {
                    appendLog("onBatchScanResults" + DateUtils.convertDateToStr(DateUtils.now(), FORMAT) + "\n");
                }

                @Override
                public void onScanFailed(int errorCode) {
                    appendLog("onScanFailed,errorCode:");
                    appendLog(errorCode + DateUtils.convertDateToStr(DateUtils.now(), FORMAT) + "\n");
                    countDownLatch.countDown();
                }
            };
            scanner.startScan(filters, settings, scanCallback);

            new Thread(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
                scanner.stopScan(scanCallback);
            }).start();
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return mDevice;
        }

        @Override
        protected void onPostExecute(BluetoothDevice bluetoothDevice) {
            if (bluetoothDevice != null) {
                appendLog("找到相应设备，准备连接" + DateUtils.convertDateToStr(DateUtils.now(), FORMAT) + "\n");
                new ConnectTask(bluetoothDevice.getAddress(), UUID.fromString("000018bb-0000-1000-8000-00805f9b34fb")).execute();
            } else {
                hideProgress();
                appendLog("未找到相应设备" + DateUtils.convertDateToStr(DateUtils.now(), FORMAT) + "\n");
            }
        }
    }

    private class ConnectTask extends AsyncTask<Void, Integer, Boolean> {

        private String mMac;
        private UUID mServiceUUID;
        private long mLastTime;

        ConnectTask(String mMac, UUID mServiceUUID) {
            this.mMac = mMac;
            this.mServiceUUID = mServiceUUID;
        }

        @Override
        protected void onPreExecute() {
            showProgress();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            mLastTime = 0;
            appendLog("连接开始" + countTime() + "\n");
            BleClient bleClient = new BleClient(TestActivity.this, mMac,
                    mServiceUUID, null, null, null, null);
            if (!bleClient.connect()) {
                bleClient.disconnect();
                bleClient.release();
                appendLog("连接失败" + countTime() + "\n");
                return false;
            }
            appendLog("连接成功， 开始查找服务" + countTime() + "\n");
            if (!bleClient.discoverServices()) {
                bleClient.disconnect();
                bleClient.release();
                appendLog("查找服务失败" + countTime() + "\n");
                return false;
            }
            appendLog("查找服务成功,发送数据开始" + countTime() + "\n");
            boolean result = bleClient.sendData(Utils.hexStringToHexByte("00112233445566778899aabbccddeeff"));
            appendLog("发送数据" + (result ? "成功" : "失败") + countTime() + "\n");
            bleClient.disconnect();
            bleClient.release();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            hideProgress();
        }

        private String countTime() {
            Date date = DateUtils.now();
            String result = DateUtils.convertDateToStr(date, FORMAT);
            if (mLastTime != 0) {
                result = result + " 用时:" + (date.getTime() - mLastTime) + "毫秒";
            }
            mLastTime = date.getTime();
            return result;
        }
    }

}

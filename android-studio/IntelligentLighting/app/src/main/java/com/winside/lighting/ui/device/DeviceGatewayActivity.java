package com.winside.lighting.ui.device;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.data.network.entity.Device;
import com.winside.lighting.mesh.WTaskGattConfig;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DeviceGatewayActivity extends BaseActivity {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.tv_navigation_right)
    TextView tv_navigation_right;

    @BindView(R.id.tv_total_device_count)
    TextView tv_total_device_count;
    @BindView(R.id.tv_lighting_total_count)
    TextView tv_lighting_total_count;
    @BindView(R.id.tv_lighting_added_count)
    TextView tv_lighting_added_count;
    @BindView(R.id.tv_lighting_reg_count)
    TextView tv_lighting_reg_count;
    @BindView(R.id.tv_lighting_online_count)
    TextView tv_lighting_online_count;
    @BindView(R.id.tv_lighting_offline_count)
    TextView tv_lighting_offline_count;

    @BindView(R.id.tv_sensor_total_count)
    TextView tv_sensor_total_count;
    @BindView(R.id.tv_sensor_added_count)
    TextView tv_sensor_added_count;
    @BindView(R.id.tv_sensor_reg_count)
    TextView tv_sensor_reg_count;
    @BindView(R.id.tv_sensor_online_count)
    TextView tv_sensor_online_count;
    @BindView(R.id.tv_sensor_offline_count)
    TextView tv_sensor_offline_count;

    @BindView(R.id.tv_switch_total_count)
    TextView tv_switch_total_count;
    @BindView(R.id.tv_switch_added_count)
    TextView tv_switch_added_count;
    @BindView(R.id.tv_switch_reg_count)
    TextView tv_switch_reg_count;
    @BindView(R.id.tv_switch_online_count)
    TextView tv_switch_online_count;
    @BindView(R.id.tv_switch_offline_count)
    TextView tv_switch_offline_count;

    @BindView(R.id.btn_config_device)
    Button btn_config_device;
    @BindView(R.id.btn_remove_device)
    Button btn_remove_device;

    private Long mDeviceId;
    private Long mCoordinateId;
    private Device mDevice;

    private List<Device> mUnRegDevices;
    private List<Device> mSuccessDevices;

    private Disposable mDisposableNoticeConfig;
    private Disposable mDisposableDeleteDevice;
    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_gateway);
        ButterKnife.bind(this);
        mDeviceId = getIntent().getLongExtra("device_id", 0);
        mCoordinateId = getIntent().getLongExtra("coordinate_id", 0);
        String deviceTypeName = getIntent().getStringExtra("device_type_name");

        btn_back.setVisibility(View.VISIBLE);
//        tv_navigation_right.setVisibility(View.VISIBLE);
        tv_navigation_title.setText(deviceTypeName);
        tv_navigation_right.setText("网关设置");
        btn_back.setOnClickListener(v -> onBackPressed());
        tv_navigation_right.setOnClickListener(v -> configAll());
        btn_config_device.setOnClickListener(v -> configDevice());
        btn_remove_device.setOnClickListener(v -> deleteDevice());

        setupContent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableNoticeConfig != null) {
            mDisposableNoticeConfig.dispose();
        }
        if (mDisposableDeleteDevice != null) {
            mDisposableDeleteDevice.dispose();
        }
    }

    private void setupContent() {
        showProgress();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = Single.zip(RestAPI.getInstance().lightingService().getDeviceDetail(mDeviceId),
                RestAPI.getInstance().lightingService().getDevices(mDeviceId),
                Pair::create)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                            hideProgress();
                            assert pair.first != null;
                            assert pair.second != null;
                            if (pair.first.code == 1000) {
                                mDevice = pair.first.data;

                                mUnRegDevices = new ArrayList<>();
                                int total = 0;
                                int lightingTotal = 0;
                                int lightingAdded = 0;
                                int lightingUnReg = 0;
                                int lightingOnline = 0;
                                int lightingOffline = 0;

                                int sensorTotal = 0;
                                int sensorAdded = 0;
                                int sensorUnReg = 0;
                                int sensorOnline = 0;
                                int sensorOffline = 0;

                                int switchTotal = 0;
                                int switchAdded = 0;
                                int switchUnReg = 0;
                                int switchOnline = 0;
                                int switchOffline = 0;

                                if (pair.second.data != null) {
                                    for (Device d : pair.second.data) {
                                        if (d.typeId == 2 || d.typeId == 3 || d.typeId == 4 || d.typeId == 7) {
                                            if (!TextUtils.isEmpty(d.status)) {
                                                lightingAdded++;
                                            }
                                            if (TextUtils.equals(d.status, "unreg")) {
                                                mUnRegDevices.add(d);
                                                lightingUnReg++;
                                            }
                                            if (TextUtils.equals(d.status, "online")) {
                                                lightingOnline++;
                                            }
                                            if (TextUtils.equals(d.status, "offline")) {
                                                lightingOffline++;
                                            }
                                            lightingTotal++;
                                        }
                                        if (d.typeId == 8) {
                                            if (!TextUtils.isEmpty(d.status)) {
                                                sensorAdded++;
                                            }
                                            if (TextUtils.equals(d.status, "unreg")) {
                                                mUnRegDevices.add(d);
                                                sensorUnReg++;
                                            }
                                            if (TextUtils.equals(d.status, "online")) {
                                                sensorOnline++;
                                            }
                                            if (TextUtils.equals(d.status, "offline")) {
                                                sensorOffline++;
                                            }
                                            sensorTotal++;
                                        }
                                        if (d.typeId == 6) {
                                            if (!TextUtils.isEmpty(d.status)) {
                                                switchAdded++;
                                            }
                                            if (TextUtils.equals(d.status, "unreg")) {
                                                mUnRegDevices.add(d);
                                                switchUnReg++;
                                            }
                                            if (TextUtils.equals(d.status, "online")) {
                                                switchOnline++;
                                            }
                                            if (TextUtils.equals(d.status, "offline")) {
                                                switchOffline++;
                                            }
                                            switchTotal++;
                                        }
                                        total++;
                                    }
                                }
                                tv_total_device_count.setText(getString(R.string.device_count, total));
                                tv_lighting_total_count.setText(String.valueOf(lightingTotal));
                                tv_lighting_added_count.setText(String.valueOf(lightingAdded));
                                tv_lighting_reg_count.setText(String.valueOf(lightingOnline + lightingOffline));
                                tv_lighting_online_count.setText(String.valueOf(lightingOnline));
                                tv_lighting_offline_count.setText(String.valueOf(lightingOffline));

                                tv_sensor_total_count.setText(String.valueOf(sensorTotal));
                                tv_sensor_added_count.setText(String.valueOf(sensorAdded));
                                tv_sensor_reg_count.setText(String.valueOf(sensorOnline + sensorOffline));
                                tv_sensor_online_count.setText(String.valueOf(sensorOnline));
                                tv_sensor_offline_count.setText(String.valueOf(sensorOffline));

                                tv_switch_total_count.setText(String.valueOf(switchTotal));
                                tv_switch_added_count.setText(String.valueOf(switchAdded));
                                tv_switch_reg_count.setText(String.valueOf(switchOnline + switchOffline));
                                tv_switch_online_count.setText(String.valueOf(switchOnline));
                                tv_switch_offline_count.setText(String.valueOf(switchOffline));

                                if (TextUtils.equals(mDevice.status, "unconf")) {
                                    btn_config_device.setText(R.string.config_device);
                                } else {
                                    btn_config_device.setText(R.string.re_config_device);
                                }
                            } else {
                                Toast.makeText(App.getInstance(), pair.first.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

    @SuppressLint("StaticFieldLeak")
    private void configDevice() {
        new InputWifiInfoFragment()
                .setListener((wifiSSID, wifiPwd) -> new WTaskGattConfig(DeviceGatewayActivity.this,
                        Utils.formatMac(mDevice.mac),
                        Utils.short_to_bb_le((short) Integer.parseInt(mDevice.meshAddr)), // 小端格式
                        mDevice.deviceKey.getBytes(),
                        Utils.convertAddresses(mDevice.fullUniCast), // 小端格式
                        Utils.int_to_bb_le(mDevice.ivIndex.intValue()), // 小端格式 (四个字节)
                        new byte[]{mDevice.ivState.byteValue()}, // 一个字节
                        Utils.short_to_bb_le(mDevice.netKeyIndex),// 小端格式
                        mDevice.netKey.getBytes(),
                        Utils.short_to_bb_le(mDevice.appKeyIndex),// 小端格式
                        mDevice.appKey.getBytes(),
                        Utils.convertAddresses(mDevice.fullMultiCast), // 小端格式
                        wifiSSID.getBytes(),
                        wifiPwd.getBytes()) {
                    @Override
                    protected void onPreExecute() {
                        showProgress();
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        hideProgress();
                        if (aBoolean) {
                            noticeConfigResult();
                        } else {
                            Toast.makeText(App.getInstance(), "设配配置失败，请重试", Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute())
                .show(getSupportFragmentManager(), "InputWifiInfoFragment");
    }

    private void noticeConfigResult() {
        showProgress();
        mDisposableNoticeConfig = RestAPI.getInstance().lightingService().noticeConfigResult(String.valueOf(mDevice.id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                    hideProgress();
                    if (objectResponseData.code == 1000) {
                        SharedPreferencesDataSource.putNeedRefresh(true);
                        Toast.makeText(App.getInstance(), "设配配置成功", Toast.LENGTH_LONG).show();
                        setupContent();
                    } else {
                        Toast.makeText(App.getInstance(), objectResponseData.message, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    hideProgress();
                    Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deleteDevice() {
        showProgress();
        mDisposableDeleteDevice = RestAPI.getInstance().lightingService().deleteDevice(mCoordinateId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                    hideProgress();
                    if (objectResponseData.code == 1000) {
                        SharedPreferencesDataSource.putNeedRefresh(true);
                        Toast.makeText(App.getInstance(), "设配删除成功", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(App.getInstance(), objectResponseData.message, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    hideProgress();
                    Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void noticeConfigAllResult() {
        showProgress();
        mDisposableNoticeConfig = RestAPI.getInstance().lightingService().noticeConfigResult(TextUtils.join(",", mSuccessDevices))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                    hideProgress();
                    if (objectResponseData.code == 1000) {
                        SharedPreferencesDataSource.putNeedRefresh(true);
                        Toast.makeText(App.getInstance(), "设配配置成功", Toast.LENGTH_LONG).show();
                        setupContent();
                    } else {
                        Toast.makeText(App.getInstance(), objectResponseData.message, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    hideProgress();
                    Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @SuppressLint("StaticFieldLeak")
    private void configAll() {
        if (mUnRegDevices != null && !mUnRegDevices.isEmpty()) {
            mSuccessDevices = new CopyOnWriteArrayList<>();
            CountDownLatch countDownLatch = new CountDownLatch(mUnRegDevices.size());
            for (Device d : mUnRegDevices) {
                new WTaskGattConfig(this,
                        Utils.formatMac(mDevice.mac),
                        Utils.short_to_bb_le((short) Integer.parseInt(mDevice.meshAddr)), // 小端格式
                        mDevice.deviceKey.getBytes(),
                        Utils.convertAddresses(mDevice.fullUniCast), // 小端格式
                        Utils.int_to_bb_le(mDevice.ivIndex.intValue()), // 小端格式 (四个字节)
                        new byte[]{mDevice.ivState.byteValue()}, // 一个字节
                        Utils.short_to_bb_le(mDevice.netKeyIndex),// 小端格式
                        mDevice.netKey.getBytes(),
                        Utils.short_to_bb_le(mDevice.appKeyIndex),// 小端格式
                        mDevice.appKey.getBytes(),
                        Utils.convertAddresses(mDevice.fullMultiCast), // 小端格式
                        null,
                        null) {
                    @Override
                    protected void onPreExecute() {
                        showProgress();
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        countDownLatch.countDown();
                        if (aBoolean) {
                            mSuccessDevices.add(d);
                        }
                    }
                }.execute();
            }

            new Thread(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(() -> {
                    hideProgress();
                    noticeConfigAllResult();
                });
            }).start();
        }
    }
}

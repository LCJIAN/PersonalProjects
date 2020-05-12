package com.winside.lighting.ui.device;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.data.network.entity.Device;
import com.winside.lighting.mesh.WTaskGattConfig;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.util.Spans;
import com.winside.lighting.util.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DeviceSensorActivity extends BaseActivity {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_back)
    ImageButton btn_back;

    @BindView(R.id.tv_device_name)
    TextView tv_device_name;
    @BindView(R.id.tv_device_mac)
    TextView tv_device_mac;
    @BindView(R.id.tv_device_sn)
    TextView tv_device_sn;
    @BindView(R.id.tv_device_rssi)
    TextView tv_device_rssi;
    @BindView(R.id.tv_temperature)
    TextView tv_temperature;
    @BindView(R.id.tv_humidity)
    TextView tv_humidity;
    @BindView(R.id.tv_illuminance)
    TextView tv_illuminance;
    @BindView(R.id.btn_config_device)
    Button btn_config_device;
    @BindView(R.id.btn_remove_device)
    Button btn_remove_device;

    private Long mDeviceId;
    private Long mCoordinateId;
    private Device mDevice;

    private Disposable mDisposable;
    private Disposable mDisposableNoticeConfig;
    private Disposable mDisposableDelete;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_sensor);
        ButterKnife.bind(this);
        mDeviceId = getIntent().getLongExtra("device_id", 0);
        mCoordinateId = getIntent().getLongExtra("coordinate_id", 0);
        String deviceTypeName = getIntent().getStringExtra("device_type_name");

        btn_back.setVisibility(View.VISIBLE);
        tv_navigation_title.setText(deviceTypeName);
        btn_back.setOnClickListener(v -> onBackPressed());
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
        if (mDisposableDelete != null) {
            mDisposableDelete.dispose();
        }
    }

    private void setupContent() {
        showProgress();
        mDisposable = RestAPI.getInstance().lightingService().getDeviceDetail(mDeviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceResponseData -> {
                            hideProgress();
                            if (deviceResponseData.code == 1000) {
                                mDevice = deviceResponseData.data;
                                tv_device_name.setText(mDevice.productName);
                                tv_device_mac.setText(mDevice.mac);
                                tv_device_sn.setText(mDevice.snCode);
                                tv_device_rssi.setText(mDevice.rssi);
                                tv_temperature.setText(new Spans()
                                        .append("温度：")
                                        .append(TextUtils.isEmpty(mDevice.attribute.get("Temperature")) ? "暂无" : mDevice.attribute.get("Temperature"),
                                                new ForegroundColorSpan(0xffeb053b)));
                                tv_humidity.setText(new Spans()
                                        .append("湿度：")
                                        .append(TextUtils.isEmpty(mDevice.attribute.get("Humidity")) ? "暂无" : mDevice.attribute.get("Humidity"),
                                                new ForegroundColorSpan(0xff057ceb)));
                                tv_illuminance.setText(new Spans()
                                        .append("光照度：")
                                        .append(TextUtils.isEmpty(mDevice.attribute.get("Illumination")) ? "暂无" : mDevice.attribute.get("Illumination"),
                                                new ForegroundColorSpan(0xffeb9205)));

                                if (TextUtils.equals(mDevice.status, "unconf")) {
                                    btn_config_device.setText(R.string.config_device);
                                } else {
                                    btn_config_device.setText(R.string.re_config_device);
                                }
                            } else {
                                Toast.makeText(App.getInstance(), deviceResponseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

    @SuppressLint("StaticFieldLeak")
    private void configDevice() {
        new WTaskGattConfig(this,
                Utils.formatMac(mDevice.mac),
                Utils.short_to_bb_le((short) Integer.parseInt(mDevice.fullUniCast)), // 小端格式
                mDevice.deviceKey.getBytes(),
                Utils.short_to_bb_le((short) Integer.parseInt(mDevice.fullUniCast)), // 小端格式
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
                hideProgress();
                if (aBoolean) {
                    noticeConfigResult();
                } else {
                    Toast.makeText(App.getInstance(), "设配配置失败，请重试", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
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
        mDisposableDelete = RestAPI.getInstance().lightingService().deleteDevice(mCoordinateId)
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
}

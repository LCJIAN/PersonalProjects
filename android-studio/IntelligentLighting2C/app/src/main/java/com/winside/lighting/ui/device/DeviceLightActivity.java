package com.winside.lighting.ui.device;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.ColorUtils;

import com.google.gson.Gson;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.data.network.entity.Device;
import com.winside.lighting.data.network.entity.DeviceControl;
import com.winside.lighting.mesh.WTaskGattConfig;
import com.winside.lighting.mesh.WTidGenerator;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.util.Utils;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DeviceLightActivity extends BaseActivity implements ColorPickerDialogListener {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_back)
    ImageButton btn_back;

    @BindView(R.id.ll_switch)
    LinearLayout ll_switch;
    @BindView(R.id.ll_rgb)
    LinearLayout ll_rgb;
    @BindView(R.id.ll_color_temperature)
    LinearLayout ll_color_temperature;
    @BindView(R.id.ll_brightness)
    LinearLayout ll_brightness;
    @BindView(R.id.switch_device)
    SwitchCompat switch_device;
    @BindView(R.id.v_color_light)
    View v_color_light;
    @BindView(R.id.seek_bar_color_temperature)
    SeekBar seek_bar_color_temperature;
    @BindView(R.id.seek_bar_light)
    SeekBar seek_bar_light;

    @BindView(R.id.tv_device_name)
    TextView tv_device_name;
    @BindView(R.id.tv_device_mac)
    TextView tv_device_mac;
    @BindView(R.id.tv_device_sn)
    TextView tv_device_sn;
    @BindView(R.id.tv_device_rssi)
    TextView tv_device_rssi;
    @BindView(R.id.tv_device_real_time_power)
    TextView tv_device_real_time_power;
    @BindView(R.id.tv_device_work_time_long)
    TextView tv_device_work_time_long;
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
    private Disposable mDisposableSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_light);
        ButterKnife.bind(this);
        mDeviceId = getIntent().getLongExtra("device_id", 0);
        mCoordinateId = getIntent().getLongExtra("coordinate_id", 0);
        String deviceTypeName = getIntent().getStringExtra("device_type_name");

        btn_back.setVisibility(View.VISIBLE);
        tv_navigation_title.setText(deviceTypeName);
        btn_back.setOnClickListener(v -> onBackPressed());
        v_color_light.setOnClickListener(v -> ColorPickerDialog.newBuilder().show(this));
        btn_config_device.setOnClickListener(v -> configDevice());
        btn_remove_device.setOnClickListener(v -> deleteDevice());
        switch_device.setOnClickListener(v -> {
            DeviceControl deviceControl = new DeviceControl();
            deviceControl.option = "DeviceControl";
            deviceControl.NetID = "fc539e438778";
            deviceControl.unicast = Integer.parseInt(mDevice.fullUniCast);
            deviceControl.data = new DeviceControl.Data();
            deviceControl.data.opcode = "8202";
            deviceControl.data.params = (switch_device.isChecked() ? "01" : "00")
                    + Utils.byteToHexString(WTidGenerator.getInstanceFor(Utils.formatMac(mDevice.mac)).getAndIncrement());
            sendToDevice(new Gson().toJson(deviceControl));
        });
        setupContent();

        seek_bar_color_temperature.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            private boolean fromUser;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.fromUser = fromUser;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fromUser) {
                    DeviceControl deviceControl = new DeviceControl();
                    deviceControl.option = "DeviceControl";
                    deviceControl.NetID = "fc539e438778";
                    deviceControl.unicast = Integer.parseInt(mDevice.fullUniCast);
                    deviceControl.data = new DeviceControl.Data();
                    deviceControl.data.opcode = "825E";
                    deviceControl.data.params = Utils.hexByteToHexString(Utils.short_to_bb_le((short) seek_bar_light.getProgress())) // Lightness
                            + Utils.hexByteToHexString(Utils.short_to_bb_le((short) (seekBar.getProgress() + 0x0320))) // Temperature
                            + Utils.hexByteToHexString(Utils.short_to_bb_le((short) 0)) // DeUV
                            + Utils.byteToHexString(WTidGenerator.getInstanceFor(Utils.formatMac(mDevice.mac)).getAndIncrement()); // Tid
                    sendToDevice(new Gson().toJson(deviceControl));
                }
            }
        });
        seek_bar_light.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            private boolean fromUser;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.fromUser = fromUser;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fromUser) {
                    DeviceControl deviceControl = new DeviceControl();
                    deviceControl.option = "DeviceControl";
                    deviceControl.NetID = "fc539e438778";
                    deviceControl.unicast = Integer.parseInt(mDevice.fullUniCast);
                    deviceControl.data = new DeviceControl.Data();
                    deviceControl.data.opcode = "824C";
                    deviceControl.data.params = Utils.hexByteToHexString(Utils.short_to_bb_le((short) seekBar.getProgress()))
                            + Utils.byteToHexString(WTidGenerator.getInstanceFor(Utils.formatMac(mDevice.mac)).getAndIncrement());
                    sendToDevice(new Gson().toJson(deviceControl));
                }
            }
        });
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
        if (mDisposableSend != null) {
            mDisposableSend.dispose();
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
                                tv_device_real_time_power.setText(mDevice.realtimePower);
                                tv_device_work_time_long.setText(mDevice.workTimeLong);

                                switch_device.setChecked(Boolean.parseBoolean(mDevice.attribute.get("OnOff")));
                                String ctl = mDevice.attribute.get("CTL");
                                if (!TextUtils.isEmpty(ctl)) {
                                    seek_bar_color_temperature.setProgress(Integer.parseInt(ctl, 16));
                                }
                                String lightness = mDevice.attribute.get("Lightness");
                                if (!TextUtils.isEmpty(lightness)) {
                                    seek_bar_light.setProgress(Integer.parseInt(lightness, 16));
                                }

                                if (TextUtils.equals(mDevice.status, "unconf")) {
                                    btn_config_device.setText(R.string.config_device);
                                    ll_switch.setVisibility(View.GONE);
//                                    ll_rgb.setVisibility(View.GONE);
                                    ll_color_temperature.setVisibility(View.GONE);
                                    ll_brightness.setVisibility(View.GONE);
                                } else {
                                    btn_config_device.setText(R.string.re_config_device);
                                    ll_switch.setVisibility(View.VISIBLE);
//                                    ll_rgb.setVisibility(View.VISIBLE);
                                    ll_color_temperature.setVisibility(View.VISIBLE);
                                    ll_brightness.setVisibility(View.VISIBLE);
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

    private void sendToDevice(String message) {
        showProgress();
        if (mDisposableSend != null) {
            mDisposableSend.dispose();
        }
        mDisposableSend = RestAPI.getInstance().lightingService().sendToDevice(mDevice.id, message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                    hideProgress();
                    if (objectResponseData.code == 1000) {
                        Toast.makeText(App.getInstance(), "操作成功", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(App.getInstance(), objectResponseData.message, Toast.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    hideProgress();
                    Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        DeviceControl deviceControl = new DeviceControl();
        deviceControl.option = "DeviceControl";
        deviceControl.NetID = "fc539e438778";
        deviceControl.unicast = Integer.parseInt(mDevice.fullUniCast);
        deviceControl.data = new DeviceControl.Data();
        deviceControl.data.opcode = "8276";
        float[] outHsl = new float[3];
        ColorUtils.colorToHSL(color, outHsl);
        Timber.d(Arrays.toString(outHsl));
//        deviceControl.data.params = Utils.byteToHexString(WTidGenerator.getInstanceFor(Utils.formatMac(mDevice.mac)).getAndIncrement());
//        sendToDevice(new Gson().toJson(deviceControl));
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }
}

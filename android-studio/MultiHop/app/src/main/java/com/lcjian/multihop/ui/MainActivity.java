package com.lcjian.multihop.ui;

import android.Manifest;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.multihop.App;
import com.lcjian.multihop.R;
import com.lcjian.multihop.lib.Manager;
import com.lcjian.multihop.lib.Role;
import com.lcjian.multihop.lib.connect.WifiP2pGC;
import com.lcjian.multihop.lib.connect.WifiP2pGO;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_wifi_p2p_status)
    TextView tv_wifi_p2p_status;
    @BindView(R.id.tv_device_info)
    TextView tv_device_info;
    @BindView(R.id.sp_role)
    AppCompatSpinner sp_role;
    @BindView(R.id.et_next_device)
    EditText et_next_device;
    @BindView(R.id.tv_log)
    TextView tv_log;
    @BindView(R.id.btn_start)
    Button btn_start;
    @BindView(R.id.btn_chat)
    Button btn_chat;
    @BindView(R.id.fl_fragment_container)
    FrameLayout fl_fragment_container;

    private Disposable mDisposableP;
    private Manager mManager;

    private String[] mRoles = new String[]{"发送者", "转发者", "接收者"};

    private Role mRole = Role.SENDER;

    private boolean mStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Logger logger = Logger.getLogger("multi-hop");
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                tv_log.append(record.getMessage());
                tv_log.append("\n");
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        });

        {
            ArrayAdapter adapter = new ArrayAdapter<>(this,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                    mRoles);
            adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
            sp_role.setAdapter(adapter);
            sp_role.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            mRole = Role.SENDER;
                            break;
                        case 1:
                            mRole = Role.FORWARDER;
                            break;
                        case 2:
                            mRole = Role.RECEIVER;
                            et_next_device.setText("");
                            break;
                        default:
                            break;
                    }
                    validateNextDevice();
                    validateStart();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        et_next_device.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateStart();
            }
        });
        btn_start.setOnClickListener(v -> {
            RxPermissions rxPermissions = new RxPermissions(this);
            mDisposableP = rxPermissions.request(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(granted -> {
                        if (granted || !mStarted) {
                            mStarted = true;
                            mRole.setNextDeviceName(et_next_device.getEditableText().toString());
                            mManager = Manager.getInstance();
                            mManager.init(this, mRole, 8000, getExternalFilesDir("Audio"));
                            mManager.start();
                            addL();

                            validateSp();
                            validateNextDevice();
                            validateChat();
                        } else {
                            Toast.makeText(App.getInstance(), "no permissions", Toast.LENGTH_LONG).show();
                        }
                    });
        });
        btn_chat.setOnClickListener(v -> {
            fl_fragment_container.setVisibility(View.VISIBLE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fl_fragment_container, new ChatFragment())
                    .addToBackStack("ChatFragment")
                    .commit();
        });

        validateSp();
        validateNextDevice();
        validateStart();
        validateChat();
    }

    private void validateStart() {
        btn_start.setEnabled(mRole == Role.RECEIVER || (!TextUtils.isEmpty(et_next_device.getEditableText())));
    }

    private void validateChat() {
        btn_chat.setEnabled(mStarted && (mRole == Role.SENDER || mRole == Role.RECEIVER));
    }

    private void validateNextDevice() {
        et_next_device.setEnabled(!mStarted && (mRole == Role.SENDER || mRole == Role.FORWARDER));
    }

    private void validateSp() {
        sp_role.setEnabled(!mStarted);
    }

    private WifiP2pGC.GCListenerAdapter mGcListenerAdapter = new WifiP2pGC.GCListenerAdapter() {
        @Override
        public void onWifiP2pEnabled(boolean enabled) {
            tv_wifi_p2p_status.setText(enabled ? "开启" : "关闭");
        }

        @Override
        public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {
            tv_device_info.setText(wifiP2pDevice.deviceName);
        }
    };

    private WifiP2pGO.GOListenerAdapter mGoListenerAdapter = new WifiP2pGO.GOListenerAdapter() {
        @Override
        public void onWifiP2pEnabled(boolean enabled) {
            tv_wifi_p2p_status.setText(enabled ? "开启" : "关闭");
        }

        @Override
        public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {
            tv_device_info.setText(wifiP2pDevice.deviceName);
        }
    };

    private void addL() {
        if (mManager.getWifiP2pGC() != null) {
            mManager.getWifiP2pGC().addListener(mGcListenerAdapter);
        }
        if (mManager.getWifiP2pGO() != null) {
            mManager.getWifiP2pGO().addListener(mGoListenerAdapter);
        }
    }

    private void removeL() {
        if (mManager.getWifiP2pGC() != null) {
            mManager.getWifiP2pGC().removeListener(mGcListenerAdapter);
        }
        if (mManager.getWifiP2pGO() != null) {
            mManager.getWifiP2pGO().removeListener(mGoListenerAdapter);
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        if (mManager != null) {
            mManager.stop();
            removeL();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fl_fragment_container != null
                && fl_fragment_container.getVisibility() == View.VISIBLE) {
            fl_fragment_container.setVisibility(View.GONE);
        }
    }
}

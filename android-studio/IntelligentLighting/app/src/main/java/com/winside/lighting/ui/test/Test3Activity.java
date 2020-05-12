package com.winside.lighting.ui.test;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.winside.lighting.R;
import com.winside.lighting.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Test3Activity extends BaseActivity {

    @BindView(R.id.et_mac_1)
    EditText et_mac_1;
    @BindView(R.id.et_mac_2)
    EditText et_mac_2;
    @BindView(R.id.et_mac_3)
    EditText et_mac_3;
    @BindView(R.id.et_mac_4)
    EditText et_mac_4;
    @BindView(R.id.tv_status_1)
    TextView tv_status_1;
    @BindView(R.id.tv_status_2)
    TextView tv_status_2;
    @BindView(R.id.tv_status_3)
    TextView tv_status_3;
    @BindView(R.id.tv_status_4)
    TextView tv_status_4;
    @BindView(R.id.btn_1)
    Button btn_1;
    @BindView(R.id.btn_2)
    Button btn_2;
    @BindView(R.id.btn_3)
    Button btn_3;
    @BindView(R.id.btn_4)
    Button btn_4;

    private int i1;
    private int i2;
    private int i3;
    private int i4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_3);
        ButterKnife.bind(this);

        et_mac_1.setText("C3:5F:61:2B:6B:9D");
        btn_1.setOnClickListener(v ->
                new WConnector(this, et_mac_1.getEditableText().toString())
                        .setListener(new WConnector.Listener() {
                            @Override
                            public void onStartConnect() {
                                text(tv_status_1, "onStartConnect");
                            }

                            @Override
                            public void onConnected() {
                                text(tv_status_1, "onConnected");
                            }

                            @Override
                            public void onDisconnected() {
                                text(tv_status_1, "onDisconnected");
                            }

                            @Override
                            public void onStartDiscoverServices() {
                                text(tv_status_1, "onStartDiscoverServices");
                            }

                            @Override
                            public void onDiscoverServicesSuccess() {
                                text(tv_status_1, "onDiscoverServicesSuccess");
                            }

                            @Override
                            public void onDiscoverServicesFailed() {
                                text(tv_status_1, "onDiscoverServicesFailed");
                            }

                            @Override
                            public void onSendData() {
                                text(tv_status_1, "onSendData" + i1++);
                            }
                        })
                        .connect());

        et_mac_2.setText("C5:73:F2:3D:BD:7F");
        btn_2.setOnClickListener(v ->
                new WConnector(this, et_mac_2.getEditableText().toString())
                        .setListener(new WConnector.Listener() {
                            @Override
                            public void onStartConnect() {
                                text(tv_status_2, "onStartConnect");
                            }

                            @Override
                            public void onConnected() {
                                text(tv_status_2, "onConnected");
                            }

                            @Override
                            public void onDisconnected() {
                                text(tv_status_2, "onDisconnected");
                            }

                            @Override
                            public void onStartDiscoverServices() {
                                text(tv_status_2, "onStartDiscoverServices");
                            }

                            @Override
                            public void onDiscoverServicesSuccess() {
                                text(tv_status_2, "onDiscoverServicesSuccess");
                            }

                            @Override
                            public void onDiscoverServicesFailed() {
                                text(tv_status_2, "onDiscoverServicesFailed");
                            }

                            @Override
                            public void onSendData() {
                                text(tv_status_2, "onSendData" + i2++);
                            }
                        })
                        .connect());

        et_mac_3.setText("D9:76:60:4A:5F:EE");
        btn_3.setOnClickListener(v ->
                new WConnector(this, et_mac_3.getEditableText().toString())
                        .setListener(new WConnector.Listener() {
                            @Override
                            public void onStartConnect() {
                                text(tv_status_3, "onStartConnect");
                            }

                            @Override
                            public void onConnected() {
                                text(tv_status_3, "onConnected");
                            }

                            @Override
                            public void onDisconnected() {
                                text(tv_status_3, "onDisconnected");
                            }

                            @Override
                            public void onStartDiscoverServices() {
                                text(tv_status_3, "onStartDiscoverServices");
                            }

                            @Override
                            public void onDiscoverServicesSuccess() {
                                text(tv_status_3, "onDiscoverServicesSuccess");
                            }

                            @Override
                            public void onDiscoverServicesFailed() {
                                text(tv_status_3, "onDiscoverServicesFailed");
                            }

                            @Override
                            public void onSendData() {
                                text(tv_status_3, "onSendData" + i3++);
                            }
                        })
                        .connect());

        et_mac_4.setText("D0:22:17:84:E4:43");
        btn_4.setOnClickListener(v ->
                new WConnector(this, et_mac_4.getEditableText().toString())
                        .setListener(new WConnector.Listener() {
                            @Override
                            public void onStartConnect() {
                                text(tv_status_4, "onStartConnect");
                            }

                            @Override
                            public void onConnected() {
                                text(tv_status_4, "onConnected");
                            }

                            @Override
                            public void onDisconnected() {
                                text(tv_status_4, "onDisconnected");
                            }

                            @Override
                            public void onStartDiscoverServices() {
                                text(tv_status_4, "onStartDiscoverServices");
                            }

                            @Override
                            public void onDiscoverServicesSuccess() {
                                text(tv_status_4, "onDiscoverServicesSuccess");
                            }

                            @Override
                            public void onDiscoverServicesFailed() {
                                text(tv_status_4, "onDiscoverServicesFailed");
                            }

                            @Override
                            public void onSendData() {
                                text(tv_status_4, "onSendData" + i4++);
                            }
                        })
                        .connect());
    }

    private void text(TextView tv, String text) {
        tv.post(() -> tv.setText(text));
    }
}

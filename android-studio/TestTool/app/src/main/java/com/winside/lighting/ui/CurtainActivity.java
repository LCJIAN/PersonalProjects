package com.winside.lighting.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.winside.lighting.R;
import com.winside.lighting.ble.BeaconClient;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CurtainActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.btn_join_in_network)
    Button btn_join_in_network;
    @BindView(R.id.btn_leave_from_network)
    Button btn_leave_from_network;
    @BindView(R.id.btn_switch_on)
    Button btn_switch_on;
    @BindView(R.id.btn_switch_off)
    Button btn_switch_off;
    @BindView(R.id.btn_pause)
    Button btn_pause;

    private BeaconClient mBeaconClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curtain);
        ButterKnife.bind(this);

        mBeaconClient = new BeaconClient(this);

        btn_back.setVisibility(View.VISIBLE);
        tv_navigation_title.setText(R.string.curtain);

        btn_back.setOnClickListener(v -> onBackPressed());
        btn_join_in_network.setOnClickListener(v -> new Thread(() -> mBeaconClient.joinInTheNetwork()).start());
        btn_leave_from_network.setOnClickListener(v -> new Thread(() -> mBeaconClient.leaveFromTheNetwork()).start());
        btn_switch_on.setOnClickListener(v -> new Thread(() -> mBeaconClient.switchOnOff(true)).start());
        btn_switch_off.setOnClickListener(v -> new Thread(() -> mBeaconClient.switchOnOff(false)).start());
        btn_pause.setOnClickListener(v -> new Thread(() -> mBeaconClient.pause()).start());
    }


    @Override
    protected void onDestroy() {
        mBeaconClient.close();
        super.onDestroy();
    }
}

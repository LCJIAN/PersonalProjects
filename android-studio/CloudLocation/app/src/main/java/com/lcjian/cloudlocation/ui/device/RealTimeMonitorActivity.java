package com.lcjian.cloudlocation.ui.device;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.network.entity.MonitorInfo;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.home.HomeContentFragment;
import com.lcjian.cloudlocation.ui.home.HomeContentFragmentGoogle;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RealTimeMonitorActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_monitor);
        ButterKnife.bind(this);

        tv_title.setText(R.string.real_time_monitor);
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        if (TextUtils.equals("Google", mUserInfoSp.getString("map", ""))) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("HomeContentFragmentGoogle");
            if (fragment == null) {
                getSupportFragmentManager().beginTransaction().add(R.id.fl_fragment_container,
                        HomeContentFragmentGoogle.newInstance((MonitorInfo.MonitorDevice) getIntent().getSerializableExtra("monitor_device")),
                        "HomeContentFragmentGoogle").commit();
            }
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("HomeContentFragment");
            if (fragment == null) {
                getSupportFragmentManager().beginTransaction().add(R.id.fl_fragment_container,
                        HomeContentFragment.newInstance((MonitorInfo.MonitorDevice) getIntent().getSerializableExtra("monitor_device")),
                        "HomeContentFragment").commit();
            }
        }
    }
}

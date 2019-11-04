package com.lcjian.cloudlocation.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.android.pushservice.PushManager;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.network.entity.SignInInfo;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.device.DevicesActivity;
import com.lcjian.cloudlocation.ui.device.MessagesActivity;
import com.lcjian.cloudlocation.ui.user.PwdModifyActivity;
import com.lcjian.cloudlocation.ui.user.UserProfileActivity;
import com.lcjian.cloudlocation.ui.user.UserSignInActivity;

import java.util.Collections;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.tv_user_info)
    TextView tv_user_info;
    @BindView(R.id.v_divider_2)
    View v_divider_2;
    @BindView(R.id.tv_message_setting)
    TextView tv_message_setting;
    @BindView(R.id.tv_search_car)
    TextView tv_search_car;
    @BindView(R.id.v_divider_4)
    View v_divider_4;
    @BindView(R.id.tv_geo_fence)
    TextView tv_geo_fence;
    @BindView(R.id.tv_pwd_modification)
    TextView tv_pwd_modification;
    @BindView(R.id.tv_about_us)
    TextView tv_about_us;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer_layout;
    @BindView(R.id.tv_sign_out)
    TextView tv_sign_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.home_page));
        SignInInfo signInInfo = getSignInInfo();
        if (signInInfo.userInfo == null) {
            tv_user_info.setVisibility(View.GONE);
            tv_search_car.setVisibility(View.GONE);
            v_divider_2.setVisibility(View.GONE);
            v_divider_4.setVisibility(View.GONE);
            tv_user_name.setText(signInInfo.deviceInfo.deviceName);
        } else {
            tv_user_name.setText(signInInfo.userInfo.userName);
        }
        btn_nav_back.setImageResource(R.drawable.sy_cdh);
        btn_nav_right.setImageResource(R.drawable.sy_xxtz);
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_back.setOnClickListener(this);
        btn_nav_right.setOnClickListener(this);
        tv_user_info.setOnClickListener(this);
        tv_message_setting.setOnClickListener(this);
        tv_search_car.setOnClickListener(this);
        tv_geo_fence.setOnClickListener(this);
        tv_pwd_modification.setOnClickListener(this);
        tv_about_us.setOnClickListener(this);
        tv_sign_out.setOnClickListener(this);

        if (TextUtils.equals("Google", mUserInfoSp.getString("map", ""))) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("HomeContentFragmentGoogle");
            if (fragment == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fl_fragment_container, new HomeContentFragmentGoogle(), "HomeContentFragmentGoogle")
                        .commit();
            }
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("HomeContentFragment");
            if (fragment == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fl_fragment_container, new HomeContentFragment(), "HomeContentFragment")
                        .commit();
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_nav_back:
                if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                    drawer_layout.closeDrawer(GravityCompat.START);
                } else {
                    drawer_layout.openDrawer(GravityCompat.START);
                }
                break;
            case R.id.btn_nav_right:
                startActivity(new Intent(this, MessagesActivity.class));
                break;
            case R.id.tv_user_info:
                startActivity(new Intent(this, UserProfileActivity.class));
                break;
            case R.id.tv_message_setting:
                mRxBus.send(new MessageSettingEvent());
                break;
            case R.id.tv_search_car:
                if (TextUtils.equals("Google", mUserInfoSp.getString("map", "Google"))) {
                    HomeContentFragmentGoogle fragment = (HomeContentFragmentGoogle) getSupportFragmentManager().findFragmentByTag("HomeContentFragmentGoogle");
                    if (fragment != null) {
                        fragment.startActivityForResult(new Intent(v.getContext(), DevicesActivity.class), 1000);
                    }
                } else {
                    HomeContentFragment fragment = (HomeContentFragment) getSupportFragmentManager().findFragmentByTag("HomeContentFragment");
                    if (fragment != null) {
                        fragment.startActivityForResult(new Intent(v.getContext(), DevicesActivity.class), 1000);
                    }
                }
                break;
            case R.id.tv_geo_fence:
                mRxBus.send(new GEOFenceEvent());
                break;
            case R.id.tv_pwd_modification:
                startActivity(new Intent(this, PwdModifyActivity.class));
                break;
            case R.id.tv_sign_out:
                mSettingSp.edit().putBoolean("auto_sign_in", false).apply();
                PushManager.delTags(this, Collections.singletonList(getSignInInfo().deviceInfo != null
                        ? "D" + getSignInInfo().deviceInfo.deviceID
                        : "U" + getSignInInfo().userInfo.userID));
                PushManager.stopWork(getApplicationContext());
                startActivity(Intent.makeRestartActivityTask(new Intent(this, UserSignInActivity.class).getComponent()));
                break;
            default:
                break;
        }
    }

    class GEOFenceEvent {

    }

    class MessageSettingEvent {

    }
}

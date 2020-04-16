package com.lcjian.cloudlocation.ui.web;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.franmontiel.localechanger.LocaleChanger;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebSettingsImpl;
import com.lcjian.cloudlocation.Constants;
import com.lcjian.cloudlocation.Global;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.device.CommandHistoryActivity;

import java.util.Locale;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SendCommandActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;
    @BindView(R.id.srl_web)
    SwipeRefreshLayout srl_web;

    private AgentWeb mAgentWeb;

    private String mDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);
        mDeviceId = getIntent().getStringExtra("device_id");

        tv_title.setText(R.string.send_command);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.mlxf_px);
        btn_nav_right.setOnClickListener(v ->
                v.getContext().startActivity(new Intent(v.getContext(), CommandHistoryActivity.class)
                        .putExtra("device_id", mDeviceId))
        );

        mAgentWeb = AgentWeb.with(this) // 传入Activity
                .setAgentWebParent(srl_web,
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)) // 传入AgentWeb的父控件
                .useDefaultIndicator() // 使用默认进度条
                .setAgentWebWebSettings(new AgentWebSettingsImpl() {
                    @Override
                    public WebSettings getWebSettings() {
                        WebSettings webSettings = super.getWebSettings();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            webSettings.setAllowFileAccessFromFileURLs(true);
                            webSettings.setAllowUniversalAccessFromFileURLs(true);
                        }
                        return webSettings;
                    }
                })
                .createAgentWeb()
                .ready()
                .go("http://" + Global.getServerUrl() + "/H5APP/cmd.aspx?" +
                        "userid=" + (getSignInInfo().userInfo == null ? "" : getSignInInfo().userInfo.userID) +
                        "&deviceID=" + mDeviceId +
                        "&key=" + Constants.KEY +
                        "&Language=" + (Locale.SIMPLIFIED_CHINESE.equals(LocaleChanger.getLocale()) ? "CN"
                        : (Locale.ENGLISH.equals(LocaleChanger.getLocale()) ? "EN" : "ES")));

        srl_web.setColorSchemeResources(R.color.colorPrimary);
        srl_web.setOnRefreshListener(this);
        srl_web.setOnChildScrollUpCallback((parent, child) -> mAgentWeb.getWebCreator().getWebView().getScrollY() > 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mAgentWeb.handleKeyEvent(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!mAgentWeb.back()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    @Override
    public void onRefresh() {
        mAgentWeb.getUrlLoader().reload();
        srl_web.postDelayed(() -> srl_web.setRefreshing(false), 2000);
    }
}


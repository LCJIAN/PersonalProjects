package com.org.firefighting.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebSettingsImpl;
import com.lcjian.lib.ui.systemuivis.SystemUiHelper;
import com.org.firefighting.R;
import com.org.firefighting.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewActivityH extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.srl_web)
    SwipeRefreshLayout srl_web;

    private AgentWeb mAgentWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_h);
        ButterKnife.bind(this);

        btn_nav_back.setOnClickListener(v -> onBackPressed());

        mAgentWeb = AgentWeb.with(this) // 传入Activity
                .setAgentWebParent(srl_web,
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)) // 传入AgentWeb的父控件
                .useDefaultIndicator() // 使用默认进度条
                .setAgentWebWebSettings(new AgentWebSettingsImpl() {
                    @Override
                    public WebSettings getWebSettings() {
                        WebSettings webSettings = super.getWebSettings();
                        webSettings.setAllowFileAccessFromFileURLs(true);
                        webSettings.setAllowUniversalAccessFromFileURLs(true);

                        String ua = getIntent().getStringExtra("user_agent");
                        if (!TextUtils.isEmpty(ua)) {
                            webSettings.setUserAgentString(ua);
                        }

                        return webSettings;
                    }
                })
                .createAgentWeb()
                .ready()
                .go(getIntent().getStringExtra("url"));

        srl_web.setColorSchemeResources(R.color.colorPrimary);
        srl_web.setOnRefreshListener(this);
        srl_web.setOnChildScrollUpCallback((parent, child) -> mAgentWeb.getWebCreator().getWebView().getScrollY() > 0);
        new SystemUiHelper(this, SystemUiHelper.LEVEL_IMMERSIVE, SystemUiHelper.FLAG_IMMERSIVE_STICKY, null).hide();
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
    public void onRefresh() {
        mAgentWeb.getUrlLoader().reload();
        srl_web.postDelayed(() -> srl_web.setRefreshing(false), 2000);
    }
}

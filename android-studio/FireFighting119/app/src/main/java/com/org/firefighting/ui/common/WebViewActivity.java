package com.org.firefighting.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebSettingsImpl;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.org.firefighting.R;
import com.org.firefighting.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.srl_web)
    SwipeRefreshLayout srl_web;

    private String mTitle;

    private AgentWeb mAgentWeb;

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            //do your work
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (TextUtils.isEmpty(mTitle)) {
                tv_title.setText(title);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);

        btn_nav_back.setOnClickListener(v -> onBackPressed());
        mTitle = getIntent().getStringExtra("title");
        if (!TextUtils.isEmpty(mTitle)) {
            tv_title.setText(mTitle);
        }

        mAgentWeb = AgentWeb.with(this) // 传入Activity
                .setAgentWebParent(srl_web,
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)) // 传入AgentWeb的父控件
                .useDefaultIndicator() // 使用默认进度条
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        if (url.contains("weixin")) {
                            return true;
                        }
                        return super.shouldOverrideUrlLoading(view, url);
                    }

                })
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
        srl_web.setEnabled(!getIntent().getBooleanExtra("swipe_disabled", false));

        srl_web.postDelayed(() -> mAgentWeb.getIndicatorController().setProgress(100), 6000);
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

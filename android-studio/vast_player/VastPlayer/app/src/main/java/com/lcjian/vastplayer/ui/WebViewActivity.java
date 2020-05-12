package com.lcjian.vastplayer.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebSettingsImpl;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.VideoUrl;
import com.lcjian.vastplayer.ui.base.BaseActivity;
import com.lcjian.vastplayer.ui.player.VideoPlayerActivity;
import com.qq.e.ads.interstitial.AbstractInterstitialADListener;
import com.qq.e.ads.interstitial.InterstitialAD;
import com.qq.e.comm.util.AdError;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.srl_web)
    SwipeRefreshLayout srl_web;
    @BindView(R.id.fab_go_to_web_play)
    FloatingActionButton fab_go_to_web_play;
    private String mCurrentUrl;

    private AgentWeb mAgentWeb;

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //do your work
            mCurrentUrl = url;
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            //do your work
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAgentWeb = AgentWeb.with(this) // 传入Activity
                .setAgentWebParent(srl_web,
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)) // 传入AgentWeb的父控件
                .useDefaultIndicator() // 使用默认进度条
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(mWebViewClient)
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
                .go(getIntent().getStringExtra("url"));

        fab_go_to_web_play.setOnClickListener(this);
        srl_web.setColorSchemeResources(R.color.primary);
        srl_web.setOnRefreshListener(this);
        srl_web.setOnChildScrollUpCallback((parent, child) -> mAgentWeb.getWebCreator().getWebView().getScrollY() > 0);

        loadAD();
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
            case R.id.fab_go_to_web_play:
                VideoUrl videoUrl = new VideoUrl();
                videoUrl.type = "web";
                videoUrl.url = mCurrentUrl;
                startActivity(new Intent(v.getContext(), VideoPlayerActivity.class)
                        .putExtra("video_url", videoUrl)
                        .putExtra("title", getSupportActionBar().getTitle()));
                break;
            default:
                break;
        }
    }

    @Override
    public void onRefresh() {
        mAgentWeb.getUrlLoader().reload();
        srl_web.postDelayed(() -> srl_web.setRefreshing(false), 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.action_close: {
                finish();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadAD() {
        final InterstitialAD ad = new InterstitialAD(this, Constants.QQ_ID, Constants.GTD_WEB_INTERSTITIAL_ID);
        ad.setADListener(new AbstractInterstitialADListener() {
            @Override
            public void onNoAD(AdError adError) {
                loadAD();
            }

            @Override
            public void onADReceive() {
                ad.show();
            }
        });
        ad.loadAD();
    }
}

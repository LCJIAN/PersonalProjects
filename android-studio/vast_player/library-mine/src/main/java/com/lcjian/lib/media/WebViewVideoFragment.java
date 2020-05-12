package com.lcjian.lib.media;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.lcjian.lib.R;

import java.util.List;

public class WebViewVideoFragment extends Fragment {

    private View mView;
    private FrameLayout mFullscreenContainer;
    private FrameLayout mContentView;
    private View mCustomView = null;
    private WebView mWebView;

    private WebChromeClient chromeClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_webview_video, container, false);
            initViews();
            initWebView();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                getActivity().getWindow().setFlags(0x1000000, 0x1000000);
            }
            mWebView.loadUrl(getArguments().getString("path"));
        } else {
            ((ViewGroup) mView.getParent()).removeView(mView);
        }
        return mView;
    }

    private void initViews() {
        mFullscreenContainer = (FrameLayout) mView.findViewById(R.id.fullscreen_custom_content);
        mContentView = (FrameLayout) mView.findViewById(R.id.main_content);
        mWebView = (WebView) mView.findViewById(R.id.webview_player);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setPluginState(PluginState.ON);
        settings.setAllowFileAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAppCacheEnabled(true);
        settings.setSaveFormData(true);

        chromeClient = new MyWebChromeClient();
        mWebView.setWebChromeClient(chromeClient);
        mWebView.setWebViewClient(new MyWebViewClient());
    }

    public void hideCustomView() {
        if (mCustomView != null) {
            chromeClient.onHideCustomView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndInstallFlash();
        mWebView.onResume();
    }

    private boolean checkFlashPlayer() {
        PackageManager packageManager = getActivity().getPackageManager();
        List<PackageInfo> infoList = packageManager.getInstalledPackages(PackageManager.GET_SERVICES);
        for (PackageInfo info : infoList) {
            if ("com.adobe.flashplayer".equals(info.packageName)) {
                return true;
            }
        }
        return false;
    }

    private void checkAndInstallFlash() {
        if (!checkFlashPlayer()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setIcon(getActivity().getResources().getIdentifier("ic_launcher", "drawable", getActivity().getPackageName()));
            builder.setTitle(R.string.app_name);
            builder.setMessage("你没有安装FlashPlayer，是否现在安装?");
            builder.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent installIntent = new Intent("android.intent.action.VIEW");
                            installIntent.setData(Uri.parse("market://details?id=com.adobe.flashplayer"));
                            startActivity(installIntent);
                        }
                    });
            builder.setNegativeButton("取消", null);
            builder.create().show();
        }
    }

    class MyWebChromeClient extends WebChromeClient {

        private CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation = getActivity().getResources().getConfiguration().orientation;

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            onShowCustomView(view, mOriginalOrientation, callback);
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation,
                                     CustomViewCallback callback) {
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mFullscreenContainer.addView(view);
                mCustomView = view;
                mCustomViewCallback = callback;
                mContentView.setVisibility(View.INVISIBLE);
                mFullscreenContainer.setVisibility(View.VISIBLE);
                mFullscreenContainer.bringToFront();
                getActivity().setRequestedOrientation(requestedOrientation);
            }
        }

        @Override
        public void onHideCustomView() {
            mContentView.setVisibility(View.VISIBLE);
            if (mCustomView == null) {
                return;
            }
            mCustomView.setVisibility(View.GONE);
            mFullscreenContainer.removeView(mCustomView);
            mCustomView = null;
            mFullscreenContainer.setVisibility(View.GONE);
            try {
                mCustomViewCallback.onCustomViewHidden();
            } catch (Exception e) {
            }
            // Show the content view.
            getActivity().setRequestedOrientation(mOriginalOrientation);
        }

        @Override
        public View getVideoLoadingProgressView() {
            return super.getVideoLoadingProgressView();
        }
    }

    class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}

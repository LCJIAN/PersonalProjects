package com.lcjian.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VideoSniffer {

    private ExecutorService mExecutor;
    private ScheduledExecutorService mCompleteChecker;
    private WebView mWebView;
    private Listener mListener;
    private String mUrl;
    private UrlFilter mUrlFilter;

    private List<String> mResult;

    private Handler mHandler;
    private boolean mCancelFlag;

    @SuppressLint("SetJavaScriptEnabled")
    public VideoSniffer(Context context, String url, boolean mobile, Listener listener) {
        mResult = new ArrayList<>();
        mUrl = url;
        mListener = listener;
        mExecutor = Executors.newSingleThreadExecutor();
        mCompleteChecker = Executors.newSingleThreadScheduledExecutor();
        mHandler = new Handler(Looper.getMainLooper());
        mUrlFilter = new FileExtensionUrlFilter("html")
                .or(new FileExtensionUrlFilter("htm"))
                .or(new FileExtensionUrlFilter("js"))
                .or(new FileExtensionUrlFilter("css"))
                .or(new FileExtensionUrlFilter("jpg"))
                .or(new FileExtensionUrlFilter("png"))
                .or(new FileExtensionUrlFilter("bmp"))
                .or(new FileExtensionUrlFilter("ico"))
                .or(new FileExtensionUrlFilter("gif"))
                .or(new FileExtensionUrlFilter("ttf"));
        mWebView = new WebView(context.getApplicationContext());
        mWebView.setLayoutParams(new ViewGroup.LayoutParams(mobile ? 1080 : 1920, mobile ? 1920 : 1080));
        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(context.getApplicationContext().getCacheDir().getPath());
        webSettings.setAppCacheMaxSize(20 * 1024 * 1024);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (mobile) {
            webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5 Build/M4B30Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.111 Mobile Safari/537.36");
        } else {
            webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
        }

        mWebView.setWebViewClient(new WebViewClient() {

            private boolean loadingFinished = true;
            private boolean redirect = false;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }

                loadingFinished = false;
                mWebView.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingFinished = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!redirect) {
                    loadingFinished = true;
                }

                if (loadingFinished && !redirect) {
                    // delay to make sure that this url is finished.
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            release(false);
                        }
                    }, 10000);
                    Log.d("VideoSniffer", "page finished");
                } else {
                    redirect = false;
                }
            }

            @Override
            public void onLoadResource(WebView view, final String url) {
                super.onLoadResource(view, url);
                Log.d("VideoSniffer", "onLoadResource " + url);
                if (!mUrlFilter.accept(url)) {
                    if (mExecutor.isShutdown()) {
                        return;
                    }
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (!mResult.contains(url)) {
                                HttpURLConnection connection = buildConnection(url, null, "GET");
                                if (connection == null) {
                                    return;
                                }
                                String contentType = connection.getHeaderField("Content-Type");
                                if (!TextUtils.isEmpty(contentType)
                                        && (contentType.contains("video")
                                        || contentType.contains("mp4")
                                        || contentType.contains("mpeg")
                                        || TextUtils.equals(contentType, "application/octet-stream"))) {
                                    mResult.add(url);
                                    notifySuccess(url);
                                } else {
                                    if (url.contains("m3u8")
                                            && (!TextUtils.isEmpty(contentType) && contentType.contains("text/html"))) { // try this
                                        String content = getContent(connection);
                                        if (!TextUtils.isEmpty(content) && content.contains("#EXTM3U")) {
                                            mResult.add(url);
                                            notifySuccess(url);
                                        }
                                    }
                                }
                                connection.disconnect();
                            }
                        }
                    });
                }
            }
        });
    }

    public void start() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mWebView.resumeTimers();
                mWebView.loadUrl(mUrl);
                mCompleteChecker.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        if (mExecutor.isTerminated()) {
                            notifyCompleted();
                            mCompleteChecker.shutdown();
                        }
                    }
                }, 1, 1, TimeUnit.SECONDS);
                if (mListener != null) {
                    mListener.onSniffStarted();
                }
                Log.d("VideoSniffer", "sniff_started");
            }
        });
    }

    public void cancel() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                release(true);
            }
        });
    }

    private void release(boolean cancel) {
        if (mWebView != null) {
            mCancelFlag = cancel;
            mExecutor.shutdown();
            mWebView.loadUrl("about:blank");
            mWebView.stopLoading();
            if (mWebView.getHandler() != null) {
                mWebView.getHandler().removeCallbacksAndMessages(null);
            }
            mWebView.removeAllViews();
            mWebView.setTag(null);
            mWebView.clearHistory();
            mWebView.destroy();
            mWebView = null;
        }
    }

    private void notifySuccess(final String url) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onSuccess(url);
                }
                Log.d("VideoSniffer", url);
            }
        });
    }

    private void notifyCompleted() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCancelFlag) {
                    if (mListener != null) {
                        mListener.onSniffCanceled();
                    }
                    Log.d("VideoSniffer", "sniff_canceled");
                } else {
                    if (mListener != null) {
                        mListener.onSniffFinished();
                    }
                    Log.d("VideoSniffer", "sniff_finished");
                }
            }
        });
    }

    public interface Listener {

        void onSniffStarted();

        void onSniffFinished();

        void onSniffCanceled();

        void onSuccess(String url);
    }

    private static HttpURLConnection buildConnection(String url, Map<String, String> headers, String method) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(10 * 1000);
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            int code = connection.getResponseCode();
            if (code == 302 || code == 301) {
                connection = buildConnection(connection.getHeaderField("Location"), headers, method);
            }
        } catch (IOException ignore) {
        }
        return connection;
    }

    private static String getContent(HttpURLConnection connection) {
        InputStream is = null;
        OutputStream os = null;
        String content = null;
        int i;
        try {
            is = connection.getInputStream();
            os = new ByteArrayOutputStream();
            while ((i = is.read()) != -1) {
                os.write(i);
            }
            content = os.toString();
        } catch (IOException ignore) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignore) {
                }
            }
        }
        return content;
    }

    static class FileExtensionUrlFilter extends UrlFilter {

        private String mFileExtension;

        FileExtensionUrlFilter(String fileExtension) {
            this.mFileExtension = fileExtension;
        }

        @Override
        public boolean accept(String url) {
            return TextUtils.equals(mFileExtension, MimeTypeMap.getFileExtensionFromUrl(url));
        }
    }

    public static abstract class UrlFilter {

        public abstract boolean accept(String url);

        /**
         * Returns the logical AND of this and the specified filter.
         *
         * @param filter The filter to AND this filter with.
         * @return A filter where calling <code>accept()</code> returns the result of
         * <code>(this.accept() &amp;&amp; filter.accept())</code>.
         */
        public UrlFilter and(UrlFilter filter) {
            if (filter == null) {
                return this;
            }

            return new UrlFilterAnd(this, filter);
        }

        /**
         * Returns the logical OR of this and the specified filter.
         *
         * @param filter The filter to OR this filter with.
         * @return A filter where calling <code>accept()</code> returns the result of
         * <code>(this.accept() || filter.accept())</code>.
         */
        public UrlFilter or(UrlFilter filter) {
            if (filter == null) {
                return this;
            }

            return new UrlFilterOr(this, filter);
        }

        private static class UrlFilterAnd extends UrlFilter {

            private final LinkedList<UrlFilter> mFilters = new LinkedList<>();

            UrlFilterAnd(UrlFilter lhs, UrlFilter rhs) {
                mFilters.add(lhs);
                mFilters.add(rhs);
            }

            @Override
            public boolean accept(String url) {
                for (UrlFilter filter : mFilters) {
                    if (!filter.accept(url)) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public UrlFilter and(UrlFilter filter) {
                mFilters.add(filter);

                return this;
            }
        }

        private static class UrlFilterOr extends UrlFilter {

            private final LinkedList<UrlFilter> mFilters = new LinkedList<>();

            UrlFilterOr(UrlFilter lhs, UrlFilter rhs) {
                mFilters.add(lhs);
                mFilters.add(rhs);
            }

            @Override
            public boolean accept(String url) {
                for (UrlFilter filter : mFilters) {
                    if (filter.accept(url)) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public UrlFilter or(UrlFilter filter) {
                mFilters.add(filter);

                return this;
            }
        }
    }
}

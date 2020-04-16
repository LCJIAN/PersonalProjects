package com.lcjian.cloudlocation.data.network;

import android.content.Context;

import com.franmontiel.localechanger.LocaleChanger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.BuildConfig;
import com.lcjian.cloudlocation.Constants;
import com.lcjian.cloudlocation.Global;
import com.lcjian.cloudlocation.data.network.entity.SignInInfo;
import com.lcjian.cloudlocation.util.ObscuredSharedPreferences;
import com.lcjian.cloudlocation.util.StorageUtils;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import timber.log.Timber;

public class RestAPI {

    private static final int DISK_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    private static RestAPI instance;

    private Retrofit urlRetrofit;
    private UrlService urlService;

    private Retrofit retrofit;
    private CloudService cloudService;

    public static RestAPI getInstance() {
        if (instance == null) {
            synchronized (RestAPI.class) {
                if (instance == null) {
                    instance = new RestAPI();
                }
            }
        }
        return instance;
    }

    private Retrofit getUrlRetrofit() {
        if (urlRetrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .cache(getCache());
            if (BuildConfig.DEBUG) {
                clientBuilder.interceptors().add(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            urlRetrofit = new Retrofit.Builder()
                    .baseUrl(Global.GET_API_URL_URL)
                    .addConverterFactory(CGsonConverterFactory.create(new GsonBuilder().setDateFormat("MMM d, yyyy").create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(clientBuilder.build())
                    .build();
        }
        return urlRetrofit;
    }

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .cache(getCache());

            clientBuilder.interceptors().add(chain -> {
                ObscuredSharedPreferences sp = new ObscuredSharedPreferences(App.getInstance(),
                        App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE));
                SignInInfo signInInfo = new Gson().fromJson(sp.getString("sign_in_info", ""), SignInInfo.class);
                String timeZone = null;
                if (signInInfo != null) {
                    if (signInInfo.userInfo == null) {
                        timeZone = signInInfo.deviceInfo.timeZone;
                    } else {
                        timeZone = signInInfo.userInfo.timeZone;
                    }
                }
                return chain.proceed(chain.request().newBuilder()
                    .url(chain.request().url().newBuilder()
                            .addQueryParameter("Key", Constants.KEY)
                            .addQueryParameter("Language", Locale.SIMPLIFIED_CHINESE.equals(LocaleChanger.getLocale()) ? "CN"
                                    : (Locale.ENGLISH.equals(LocaleChanger.getLocale()) ? "EN" : "ES"))
                            .addQueryParameter("TimeZones", timeZone)
                            .build())
                        .build());
            });
            if (BuildConfig.DEBUG) {
                clientBuilder.interceptors().add(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(Global.getApiUrl())
                    .addConverterFactory(CGsonConverterFactory.create(new GsonBuilder().setDateFormat("MMM d, yyyy").create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(clientBuilder.build())
                    .build();
        }
        return retrofit;
    }

    private Cache getCache() {
        Cache cache = null;
        // Install an HTTP cache in the application cache directory.
        try {
            File cacheDir = new File(StorageUtils.getCacheDirectory(App.getInstance()), "http");
            cache = new Cache(cacheDir, DISK_CACHE_SIZE);
        } catch (Exception e) {
            Timber.e(e, "Unable to install disk cache.");
        }
        return cache;
    }

    public UrlService urlService() {
        if (urlService == null) {
            urlService = getUrlRetrofit().create(UrlService.class);
        }
        return urlService;
    }

    public CloudService cloudService() {
        if (cloudService == null) {
            cloudService = getRetrofit().create(CloudService.class);
        }
        return cloudService;
    }

    public void reset() {
        urlRetrofit = null;
        urlService = null;
        retrofit = null;
        cloudService = null;
    }
}

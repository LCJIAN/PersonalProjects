package com.lcjian.lib.areader.data.network;

import android.support.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.lcjian.lib.areader.App;
import com.lcjian.lib.areader.BuildConfig;
import com.lcjian.lib.areader.Constants;
import com.lcjian.lib.areader.util.MD5Utils;
import com.lcjian.lib.areader.util.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RestAPI {

    private static final String API_URL = BuildConfig.API_URL;

    private static final int DISK_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    private static RestAPI instance;
    private Retrofit retrofit;
    private ReaderService readerService;

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

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .cache(getCache());

            clientBuilder.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    String deviceId = Constants.DEVICE_ID;
                    String t = String.valueOf(System.currentTimeMillis() / 1000);
                    String token = MD5Utils.getMD532("@#$%" + deviceId + "^&*" + t);
                    return chain.proceed(chain.request().newBuilder()
                            .addHeader("user-agent", Constants.USER_AGENT)
                            .addHeader("channel", Constants.CHANNEL)
                            .addHeader("versionCode", Constants.VERSION_CODE)
                            .addHeader("deviceId", deviceId)
                            .addHeader("t", t)
                            .addHeader("token", token)
                            .build());
                }
            });
            if (BuildConfig.DEBUG) {
                clientBuilder.interceptors().add(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("MMM d, yyyy").create()))
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

    public ReaderService readerService() {
        if (readerService == null) {
            readerService = getRetrofit().create(ReaderService.class);
        }
        return readerService;
    }
}

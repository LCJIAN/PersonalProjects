package com.lcjian.vastplayer.data.network;

import android.util.Base64;

import com.google.gson.GsonBuilder;
import com.lcjian.lib.util.common.StorageUtils;
import com.lcjian.lib.util.security.MD5Utils;
import com.lcjian.vastplayer.App;
import com.lcjian.vastplayer.BuildConfig;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.data.network.service.SpunSugarService;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RestAPI {

    private static final String API_URL = BuildConfig.API_URL;

    private static final int DISK_CACHE_SIZE = 20 * 1024 * 1024; // 20MB

    private Retrofit retrofit;

    private SpunSugarService spunSugarService;

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .cache(getCache());

            clientBuilder.interceptors().add(chain -> {
                long time = System.currentTimeMillis();
                String username = chain.request().url().toString() + time;
                String password = MD5Utils.getMD532(username + Constants.SPUN_SUGAR_API_KEY);
                return chain.proceed(chain.request().newBuilder()
                        .addHeader("Authorization", "Basic " + Base64.encodeToString((username + ";" + password).getBytes(), Base64.NO_WRAP))
                        .addHeader("Date", String.valueOf(time))
                        .build());
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

    public SpunSugarService spunSugarService() {
        if (spunSugarService == null) {
            spunSugarService = getRetrofit().create(SpunSugarService.class);
        }
        return spunSugarService;
    }
}

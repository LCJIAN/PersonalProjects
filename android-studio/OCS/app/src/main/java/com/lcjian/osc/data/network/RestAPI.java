package com.lcjian.osc.data.network;

import com.google.gson.GsonBuilder;
import com.lcjian.osc.App;
import com.lcjian.osc.BuildConfig;
import com.lcjian.osc.Constants;
import com.lcjian.osc.util.StorageUtils;

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

    private static final int DISK_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    private static RestAPI instance;

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

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .cache(getCache());

            clientBuilder.interceptors().add(chain -> chain.proceed(chain.request().newBuilder()
                    .url(chain.request().url().newBuilder()
                            .addQueryParameter("Key", Constants.KEY)
                            .addQueryParameter("Language", Constants.LANGUAGE)
                            .build())
                    .build()));
            if (BuildConfig.DEBUG) {
                clientBuilder.interceptors().add(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_URL)
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

    public CloudService cloudService() {
        if (cloudService == null) {
            cloudService = getRetrofit().create(CloudService.class);
        }
        return cloudService;
    }
}

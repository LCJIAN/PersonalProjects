package com.winside.lighting.data.network;

import com.google.gson.GsonBuilder;
import com.winside.lighting.App;
import com.winside.lighting.BuildConfig;
import com.winside.lighting.data.network.entity.ResponseData;
import com.winside.lighting.data.network.entity.SignInInfo;
import com.winside.lighting.util.StorageUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RestAPI {

    private static String API_URL = BuildConfig.API_URL;

    private static final int DISK_CACHE_SIZE = 20 * 1024 * 1024; // 20MB

    private static RestAPI instance;

    private Retrofit retrofitSignIn;
    private Retrofit retrofit;

    private SignInService signInService;
    private LightingService lightingService;

    private String token;

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

    private Retrofit getRetrofitSignIn() {
        if (retrofitSignIn == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS);
            if (BuildConfig.DEBUG) {
                clientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("MMM d, yyyy").create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(clientBuilder.build())
                    .build();
        }
        return retrofitSignIn;
    }

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .cache(getCache());

            clientBuilder
                    .authenticator((route, response) -> {
                        Call<ResponseData<SignInInfo>> call = signInService().signIn("", "");
                        ResponseData<SignInInfo> responseData = call.execute().body();
                        if (responseData != null) {
                            token = responseData.data.token;
                            return response.request()
                                    .newBuilder()
                                    .addHeader("Authorization", token)
                                    .build();
                        } else {
                            return null;
                        }
                    })
                    .addInterceptor(chain -> chain.proceed(chain.request().newBuilder().addHeader("Authorization", token).build()));
            if (BuildConfig.DEBUG) {
                clientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
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

    public void resetApiUrl(String apiUrl) {
        retrofitSignIn = null;
        retrofit = null;
        token = null;
        API_URL = apiUrl;
    }

    public void refreshToken(String token) {
        this.token = token;
    }

    public LightingService lightingService() {
        if (lightingService == null) {
            lightingService = getRetrofit().create(LightingService.class);
        }
        return lightingService;
    }

    public SignInService signInService() {
        if (signInService == null) {
            signInService = getRetrofitSignIn().create(SignInService.class);
        }
        return signInService;
    }
}

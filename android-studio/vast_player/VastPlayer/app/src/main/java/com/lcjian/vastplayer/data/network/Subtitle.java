package com.lcjian.vastplayer.data.network;

import com.lcjian.vastplayer.BuildConfig;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.data.network.service.SubtitleService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Subtitle {

    public static final String API_URL = "http://api.makedie.me/v1/";

    private Retrofit retrofit;

    private SubtitleService subtitleService;

    protected Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS);
            clientBuilder.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    return chain.proceed(chain.request().newBuilder().addHeader("Authorization", "Bearer " + Constants.SHOOTER_API_KEY).build());
                }
            });
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                clientBuilder.interceptors().add(logging);
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(clientBuilder.build())
                    .build();
        }
        return retrofit;
    }

    public SubtitleService subtitleService() {
        if (subtitleService == null) {
            subtitleService = getRetrofit().create(SubtitleService.class);
        }
        return subtitleService;
    }
}

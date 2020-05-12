package com.winside.lighting.data.network;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.winside.lighting.App;
import com.winside.lighting.BuildConfig;
import com.winside.lighting.util.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
    private TokenService tokenService;

    private static final TypeAdapter<Boolean> BOOLEAN_TYPE_ADAPTER = new TypeAdapter<Boolean>() {

        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            switch (peek) {
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                case NUMBER:
                    return in.nextInt() != 0;
                case STRING:
                    String s = in.nextString();
                    if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
                        return Boolean.parseBoolean(s);
                    } else {
                        return Integer.parseInt(s) != 0;
                    }
                default:
                    throw new IllegalStateException("Expected BOOLEAN or NUMBER but was " + peek);
            }
        }
    };

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
            retrofitSignIn = new Retrofit.Builder()
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

            if (BuildConfig.DEBUG) {
                clientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory
                            .create(new GsonBuilder()
                                    .registerTypeAdapter(Boolean.class, BOOLEAN_TYPE_ADAPTER)
                                    .registerTypeAdapter(boolean.class, BOOLEAN_TYPE_ADAPTER)
                                    .setDateFormat("MMM d, yyyy")
                                    .create()))
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

    public TokenService tokenService() {
        if (tokenService == null) {
            tokenService = getRetrofit().create(TokenService.class);
        }
        return tokenService;
    }

    public SignInService signInService() {
        if (signInService == null) {
            signInService = getRetrofitSignIn().create(SignInService.class);
        }
        return signInService;
    }
}

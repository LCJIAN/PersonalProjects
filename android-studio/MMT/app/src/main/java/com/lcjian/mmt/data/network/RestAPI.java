package com.lcjian.mmt.data.network;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.GsonBuilder;
import com.lcjian.mmt.App;
import com.lcjian.mmt.BuildConfig;
import com.lcjian.mmt.Global;
import com.lcjian.mmt.data.network.entity.ResponseData;
import com.lcjian.mmt.data.network.entity.SignInRequestData;
import com.lcjian.mmt.util.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RestAPI {

    private static final int DISK_CACHE_SIZE = 20 * 1024 * 1024; // 20MB

    private SharedPreferences userInfoSp;
    private Retrofit retrofit;
    private CloudService cloudService;

    public RestAPI(SharedPreferences userInfoSp) {
        this.userInfoSp = userInfoSp;
    }

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .cache(getCache());

            clientBuilder
                    .addInterceptor(chain -> {
                        String token = userInfoSp.getString("token", "");
                        if (TextUtils.isEmpty(token)) {
                            return chain.proceed(chain.request());
                        } else {
                            return chain.proceed(chain.request()
                                    .newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build());
                        }
                    })
                    .authenticator((route, response) -> {
                        String token = signIn();
                        if (TextUtils.isEmpty(token)) {
                            return response.request();
                        } else {
                            userInfoSp.edit().putString("token", token).apply();
                            return response.request()
                                    .newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                        }
                    });
            if (BuildConfig.DEBUG) {
                clientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(Global.API_URL)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z").create()))
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

    public void reset() {
        retrofit = null;
        cloudService = null;
    }

    private String signIn() throws IOException {
        String signInAccount = userInfoSp.getString("sign_in_account", "");
        String signInPwd = userInfoSp.getString("sign_in_pwd", "");
        SignInRequestData signInRequestData = new SignInRequestData();
        signInRequestData.tenancyName = HttpUrl.get(Global.API_URL).host();
        signInRequestData.usernameOrEmailAddress = signInAccount;
        signInRequestData.password = signInPwd;
        if (TextUtils.isEmpty(signInAccount)) {
            return null;
        } else {
            Call<ResponseData<String>> call;
            call = cloudService().signInSync(signInRequestData);
            ResponseData<String> responseData = call.execute().body();
            if (responseData == null) {
                return null;
            } else {
                return responseData.result;
            }
        }
    }
}

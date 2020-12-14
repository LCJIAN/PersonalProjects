package com.org.firefighting.data.network;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lcjian.lib.util.common.StorageUtils;
import com.org.firefighting.App;
import com.org.firefighting.BuildConfig;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.entity.ResponseData;
import com.org.firefighting.data.network.entity.SignInRequest;
import com.org.firefighting.data.network.entity.SignInResponse;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class RestAPI {

    private static final String API_URL = BuildConfig.API_URL;
    private static final String API_URL_SB = BuildConfig.API_URL_SB;
    private static final String API_URL_SB_2 = BuildConfig.API_URL_SB_2;

    private static final int DISK_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    private static RestAPI instance;
    private Retrofit retrofit;
    private Retrofit retrofitSignIn;
    private Retrofit retrofitSB;
    private Retrofit retrofitSB2;

    private ApiService apiService;
    private ApiService apiServiceSignIn;
    private ApiService apiServiceSB;
    private ApiService apiServiceSB2;

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
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS);
            clientBuilder.addInterceptor(chain -> {
                SignInResponse signInResponse = SharedPreferencesDataSource.getSignInResponse();
                if (signInResponse == null) {
                    return chain.proceed(chain.request());
                } else {
                    return chain.proceed(chain.request()
                            .newBuilder()
                            .header("Authorization", "Bearer " + signInResponse.token)
                            .build());
                }
            });
            if (BuildConfig.DEBUG) {
                clientBuilder.interceptors().add(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            retrofitSignIn = new Retrofit.Builder()
                    .baseUrl(API_URL_SB)
                    .addConverterFactory(GsonConverterFactory.create(new Gson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(clientBuilder.build())
                    .build();
        }
        return retrofitSignIn;
    }

    private Retrofit getRetrofitSB() {
        if (retrofitSB == null) {
            retrofitSB = createRetrofit(API_URL_SB);
        }
        return retrofitSB;
    }

    private Retrofit getRetrofitSB2() {
        if (retrofitSB2 == null) {
            retrofitSB2 = createRetrofit(API_URL_SB_2);
        }
        return retrofitSB2;
    }

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = createRetrofit(API_URL);
        }
        return retrofit;
    }

    private Retrofit createRetrofit(String baseUrl) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .cache(getCache());
        clientBuilder
                .addNetworkInterceptor(chain -> {
                    Response response = chain.proceed(chain.request());
                    ResponseBody body = response.body();
                    if (body != null) {
                        String string = body.string();
                        try {
                            ResponseData<String> r = new Gson().fromJson(string, new TypeToken<ResponseData<String>>() {}.getType());
                            if (r != null && r.code != null && r.code == -1 && TextUtils.equals("用户未登录或token失效。", r.data)) {
                                return response.newBuilder().code(401).build();
                            } else {
                                return response.newBuilder().body(ResponseBody.create(body.contentType(), string)).build();
                            }
                        } catch (JsonSyntaxException e) {
                            return response.newBuilder().body(ResponseBody.create(body.contentType(), string)).build();
                        }
                    } else {
                        return response;
                    }
                })
                .addInterceptor(chain -> {
                    SignInResponse signInResponse = SharedPreferencesDataSource.getSignInResponse();
                    if (signInResponse == null) {
                        return chain.proceed(chain.request());
                    } else {
                        return chain.proceed(chain.request()
                                .newBuilder()
                                .header("token", signInResponse.token)
                                .header("Authorization", "Bearer " + signInResponse.token)
                                .build());
                    }
                })
                .authenticator((route, response) -> {
                    // If we've failed 3 times, give up.
                    if (responseCount(response) >= 3) {
                        return null;
                    }
                    SignInRequest signInRequest = SharedPreferencesDataSource.getSignInRequest();
                    if (signInRequest == null) {
                        return response.request();
                    } else {
                        Call<SignInResponse> call = apiServiceSignIn().signInSync(signInRequest);
                        SignInResponse signInResponse = call.execute().body();
                        if (signInResponse == null) {
                            return response.request();
                        } else {
                            SignInResponse signInResponseL = SharedPreferencesDataSource.getSignInResponse();
                            if (signInResponseL == null) {
                                SharedPreferencesDataSource.putSignInResponse(signInResponse);
                            } else {
                                signInResponseL.token = signInResponse.token;
                                signInResponseL.user = signInResponse.user;
                                SharedPreferencesDataSource.putSignInResponse(signInResponseL);
                            }
                            return response.request()
                                    .newBuilder()
                                    .header("token", signInResponse.token)
                                    .header("Authorization", "Bearer " + signInResponse.token)
                                    .build();
                        }
                    }
                });
        if (BuildConfig.DEBUG) {
            clientBuilder.interceptors().add(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(CGsonConverterFactory.create(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(clientBuilder.build())
                .build();
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    private static Cache getCache() {
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

    public ApiService apiServiceSignIn() {
        if (apiServiceSignIn == null) {
            apiServiceSignIn = getRetrofitSignIn().create(ApiService.class);
        }
        return apiServiceSignIn;
    }

    public ApiService apiServiceSB() {
        if (apiServiceSB == null) {
            apiServiceSB = getRetrofitSB().create(ApiService.class);
        }
        return apiServiceSB;
    }

    public ApiService apiServiceSB2() {
        if (apiServiceSB2 == null) {
            apiServiceSB2 = getRetrofitSB2().create(ApiService.class);
        }
        return apiServiceSB2;
    }

    public ApiService apiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService.class);
        }
        return apiService;
    }
}

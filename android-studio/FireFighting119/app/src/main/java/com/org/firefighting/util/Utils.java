package com.org.firefighting.util;

import android.text.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class Utils {

    public static String get(String url, HashMap<String, String> headers, HashMap<String, String> params) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        Response response = null;
        try {
            HttpUrl httpUrl = HttpUrl.parse(url);
            HttpUrl.Builder builder;
            if (httpUrl == null) {
                return null;
            } else {
                builder = httpUrl.newBuilder();
            }
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    builder.addQueryParameter(param.getKey(), param.getValue());
                }
            }
            Request.Builder requestBuilder = new Request.Builder().url(builder.build());
            if (headers != null && !headers.isEmpty()) {
                requestBuilder.headers(Headers.of(headers));
            }
            response = new OkHttpClient().newCall(requestBuilder.build()).execute();
            if (response.body() == null) {
                return null;
            } else {
                return response.body().string();
            }
        } catch (IOException e) {
            Timber.e(e);
            return null;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}

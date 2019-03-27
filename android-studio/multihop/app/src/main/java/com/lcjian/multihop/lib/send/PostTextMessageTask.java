package com.lcjian.multihop.lib.send;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class PostTextMessageTask implements Task {

    private String text;

    public PostTextMessageTask(String text) {
        this.text = text;
    }

    @Override
    public void run(String ip, int port) throws Exception {
        new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
                .newCall(new Request.Builder()
                        .url("http://" + ip + ":" + port + "/message/text")
                        .post(RequestBody.create(MediaType.parse("text/plain"), text))
                        .build())
                .execute();
    }

}

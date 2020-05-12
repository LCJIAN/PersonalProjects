package com.lcjian.parser.video;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import com.lcjian.okhttp.OkHttpClientSingleton;
import com.lcjian.util.StringUtils;

public class QianmoParser {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36";
    private static final String PLAY_URL = "https://dqplayers.duapp.com/bshare/bshare.php";

    public String parse(String url) {
        try {
            HttpUrl httpUrl = HttpUrl.parse(PLAY_URL).newBuilder().addEncodedQueryParameter("v", url).build();
            return StringUtils.r1(getContent(httpUrl.toString()), "var video=\\['(.*?)'\\];");
        } catch (Exception e) {
            return "";
        }
    }

    private String getContent(String url) {
        Request request = new Request.Builder().url(url).header("User-Agent", USER_AGENT).build();
        Response response;
        try {
            response = OkHttpClientSingleton.getSingleton().newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}

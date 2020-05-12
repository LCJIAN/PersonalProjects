package com.lcjian.parser.video;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.lcjian.okhttp.OkHttpClientSingleton;
import com.lcjian.util.StringUtils;

public class BiliBiliH5Parser {

    private static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_4 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11B554a Safari/9537.53";
    private static final String PLAY_URL = "http://www.bilibili.tv/m/html5";

    public String parse(String url) {
        try {
            Pattern p = Pattern
                    .compile("http:/*[^/]+/video/av(\\d+)(/|/index.html|/index_(\\d+).html)?(\\?|#|$)");
            Matcher m = p.matcher(url);
            String aid = "";
            String pid = "";
            while (m.find()) {
                aid = m.group(1);
                pid = m.group(3);
            }
            if (StringUtils.isEmpty(pid)) {
                pid = "1";
            }
            HttpUrl httpUrl = HttpUrl.parse(PLAY_URL).newBuilder()
                    .addEncodedQueryParameter("aid", aid)
                    .addEncodedQueryParameter("page", pid).build();
            return new Gson().fromJson(getContent(httpUrl.toString()), Video.class).src;
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
    
    private static class Video {
        public String src;
    }
}

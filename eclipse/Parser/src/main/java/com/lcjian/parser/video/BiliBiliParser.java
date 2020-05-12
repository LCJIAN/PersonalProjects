package com.lcjian.parser.video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.lcjian.okhttp.OkHttpClientSingleton;
import com.lcjian.util.MD5Utils;
import com.lcjian.util.StringUtils;

public class BiliBiliParser {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.99 Safari/537.36";
    private static final String APP_KEY = "43fd790e02107193";
    private static final String APP_SEC = "3c787076f6cc255d493a60077bf904ec";

    private static boolean mOverseas = false;

    public String parse(String url) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("type", "json");
            Pattern p = Pattern.compile("http:/*[^/]+/video/av(\\d+)(/|/index.html|/index_(\\d+).html)?(\\?|#|$)");
            Matcher m = p.matcher(url);
            while (m.find()) {
                params.put("id", m.group(1));
                String pid = m.group(3);
                params.put("page", pid == null ? "1" : pid);
            }
            CidInfo cidInfo = new Gson().fromJson(getContent("http://api.bilibili.com/view?" + sign(params)), CidInfo.class);
            String playUrl;
            if (mOverseas) {
                playUrl = "http://interface.bilibili.com/playurl";
            } else {
                playUrl = "http://interface.bilibili.com/v_cdn_play";
            }
            HttpUrl httpUrl = HttpUrl.parse(playUrl)
                    .newBuilder().addEncodedQueryParameter("otype", "json")
                    .addEncodedQueryParameter("cid", String.valueOf(cidInfo.cid))
                    .addEncodedQueryParameter("type", "mp4")
                    .addEncodedQueryParameter("quality", "1")
                    .addEncodedQueryParameter("appkey", APP_KEY).build();
            return StringUtils.r1(getContent(httpUrl.toString()), "\"url\":\"(.*?)\"");
        } catch (Exception e) {
            return "";
        }
    }

    private String sign(Map<String, String> params) {
        params.put("appkey", APP_KEY);
        List<String> keys = new ArrayList<String>();
        keys.addAll(params.keySet());
        Collections.sort(keys);
        StringBuilder dataBuilder = new StringBuilder();
        for (String key : keys) {
            if (!StringUtils.isEmpty(dataBuilder.toString())) {
                dataBuilder.append("&");
            }
            dataBuilder.append(key).append("=").append(params.get(key));
        }
        String sign = MD5Utils.getMD532(dataBuilder.toString() + APP_SEC);
        dataBuilder.append("&sign=").append(sign);
        return dataBuilder.toString();
    }

    private String getContent(String url) {
        String ip = "";
        if (new Random().nextInt(2) == 1) {
            ip = "220.181.111." + (new Random().nextInt(255) + 1);
        } else {
            ip = "59.152.193." + (new Random().nextInt(255) + 1);
        }
        Request request = new Request.Builder().url(url)
                .header("User-Agent", USER_AGENT)
                .header("Client-IP", ip)
                .header("X-Forwarded-For", ip).build();
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

    private static class CidInfo {
        public long cid;
    }
}

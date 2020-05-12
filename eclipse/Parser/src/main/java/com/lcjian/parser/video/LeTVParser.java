package com.lcjian.parser.video;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.lcjian.okhttp.OkHttpClientSingleton;
import com.lcjian.util.StringUtils;

public class LeTVParser {

    public String parse(String url) {
        try {
            Pattern p;
            if (url.contains("letv")) {
                p = Pattern.compile("http://www\\.letv\\.com/ptv/vplay/(\\d+)\\.html");
            } else {
                p = Pattern.compile("http://www\\.le\\.com/ptv/vplay/(\\d+)\\.html");
            }
            Matcher m = p.matcher(url);
            String vid = "";
            while (m.find()) {
                vid = m.group(1);
            }
            HttpUrl httpUrl = HttpUrl
                    .parse("http://api.le.com/mms/out/video/playJson")
                    .newBuilder()
                    .addEncodedQueryParameter("id", vid)
                    .addEncodedQueryParameter("platid", "1")
                    .addEncodedQueryParameter("splatid", "101")
                    .addEncodedQueryParameter("format", "1")
                    .addEncodedQueryParameter("tkey", String.valueOf(getTkey((long) (new Date().getTime() * 0.001))))
                    .addEncodedQueryParameter("domain", "www.le.com")
                    .addEncodedQueryParameter("accessyx", "1")
                    .build();
            return getContent(httpUrl.toString());
        } catch (Exception e) {
            return "";
        }
    }

    private String getContent(String url) {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.75 Safari/537.36")
                .header("Connection", "close")
                .header("Accept-Encoding", "identity")
                .build();
        Response response;
        try {
            response = OkHttpClientSingleton.getSingleton().newCall(request).execute();
            if (response.isSuccessful()) {
                PlayInfo playInfo = new Gson().fromJson(response.body().string(), PlayInfo.class);
                String dispatchStr;
                String dispatchName;
                if (playInfo.playurl.dispatch.dispatch350 != null && !playInfo.playurl.dispatch.dispatch350.isEmpty()) {
                    dispatchStr = playInfo.playurl.dispatch.dispatch350.get(0);
                    dispatchName = "350";
                } else if (playInfo.playurl.dispatch.dispatch1000 != null && !playInfo.playurl.dispatch.dispatch1000.isEmpty()) {
                    dispatchStr = playInfo.playurl.dispatch.dispatch1000.get(0);
                    dispatchName = "1000";
                } else if (playInfo.playurl.dispatch.dispatch1300 != null && !playInfo.playurl.dispatch.dispatch1300.isEmpty()) {
                    dispatchStr = playInfo.playurl.dispatch.dispatch1300.get(0);
                    dispatchName = "1300";
                } else if (playInfo.playurl.dispatch.dispatch720p != null && !playInfo.playurl.dispatch.dispatch720p.isEmpty()) {
                    dispatchStr = playInfo.playurl.dispatch.dispatch720p.get(0);
                    dispatchName = "720p";
                } else {
                    dispatchStr = playInfo.playurl.dispatch.dispatch1080p.get(0);
                    dispatchName = "1080p";
                }
                String m3u8RequestUrl = playInfo.playurl.domain.get(0)
                        + dispatchStr
                        + "&ctv=pc&m3v=1&termid=1&format=1&hwtype=un&ostype=Linux&tag=letv&sign=letv&expect=3&tn="
                        + new Random().nextDouble()
                        + "&pay=0&iscpn=f9051&rateid="
                        + dispatchName;
                Request m3u8Request = new Request.Builder().url(m3u8RequestUrl).build();
                response = OkHttpClientSingleton.getSingleton().newCall(m3u8Request).execute();
                if (response.isSuccessful()) {
                    return StringUtils.r1(response.body().string(), "\"location\": \"(.*?)\"");
                }
                return "";
            } else {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public long getTkey(long stime) {

        long key = 773625421;

        long value = ror(stime, key % 13);

        value = value ^ key;

        value = ror(value, key % 17);

        return value;
    }

//    private long generateKeyRor(long value, long key) {
//        int temp = (int) (Math.pow(2, 32) - 1);
//        return ((value & temp) >> key % 32) | (value << (32 - (key % 32)) & temp);
//    }
    
    private long ror(long param1, long param2) {
        long _loc3_ = 0;
        while (_loc3_ < param2) {
            param1 = (param1 >>> 1) + ((param1 & 1) << 31);
            _loc3_++;
        }
        return param1;
    }
    
    private static class PlayInfo {
        @SerializedName("playurl")
        public PlayUrl playurl;

        private static class PlayUrl {
            @SerializedName("domain")
            public List<String> domain;
            @SerializedName("dispatch")
            public Dispatch dispatch;

            private static class Dispatch {
                @SerializedName("350")
                public List<String> dispatch350;
                @SerializedName("1000")
                public List<String> dispatch1000;
                @SerializedName("1300")
                public List<String> dispatch1300;
                @SerializedName("1080p")
                public List<String> dispatch1080p;
                @SerializedName("720p")
                public List<String> dispatch720p;
            }
        }
    }
}

package com.lcjian.parser.tv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Request;
import okhttp3.Response;

import com.lcjian.okhttp.OkHttpClientSingleton;
import com.lcjian.util.StringUtils;

public class CntvParser {

    public String parse(String url) {
        try {
            String html = getContent(url);
            String liveurl1 = null;
            List<String> results = new ArrayList<String>();
            if (html != null) {
                results.add(r1(html, "hls4" + "\":\"(.*?)\""));
                results.add(r1(html, "hls1" + "\":\"(.*?)\""));
                results.add(r1(html, "hls2" + "\":\"(.*?)\""));
                results.add(r1(html, "hls6" + "\":\"(.*?)\""));
                results.add(r1(html, "flv2" + "\":\"(.*?)\""));
                results.add(r1(html, "flv4" + "\":\"(.*?)\""));
                for (String result : results) {
                    if (!StringUtils.isEmpty(result)) {
                        try {
                            if (OkHttpClientSingleton.getSingleton()
                                    .newCall(new Request.Builder().url(result).build())
                                    .execute()
                                    .isSuccessful()) {
                                liveurl1 = result;
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return liveurl1;
        } catch (Exception e) {
            return "";
        }
    }

    private String getContent(String url) {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_4 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11B554a Safari/9537.53")
                .build();
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

    public String r1(String html, String pattern) {
        String con = null;
        if (html == null)
            return con;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(html);
        while (m.find()) {
            con = m.group(1);
        }
        return con;
    }
}

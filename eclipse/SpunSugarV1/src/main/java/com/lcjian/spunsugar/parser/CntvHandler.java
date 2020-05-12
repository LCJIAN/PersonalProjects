package com.lcjian.spunsugar.parser;

import java.util.ArrayList;
import java.util.List;

import com.lcjian.spunsugar.util.StringUtils;

public class CntvHandler {

    private static final String s1 = "http://vdn.live.cntv.cn/api2/liveHtml5.do?channel=pa://cctv_p2p_hd";
    private static final String s2 = "&client=html5";

    public static String getLiveUrl(String key) {
        String key1 = key.split(":")[0];
        String key2 = key.split(":")[1];
        String url = s1 + key1 + s2;
        String html = Common.htmlget(url);
        String liveurl1 = null;
        if (html != null) {
            liveurl1 = Common.r1(html, key2 + "\":\"(.*?)\"");
        }
        return liveurl1;
    }

    public static List<String> getLiveUrl2(String html) {
        List<String> result = new ArrayList<String>();
        result.add(Common.r1(html, "hls4" + "\":\"(.*?)\""));
        result.add(Common.r1(html, "hls1" + "\":\"(.*?)\""));
        result.add(Common.r1(html, "hls2" + "\":\"(.*?)\""));
        result.add(Common.r1(html, "hls6" + "\":\"(.*?)\""));
        result.add(Common.r1(html, "flv2" + "\":\"(.*?)\""));
        result.add(Common.r1(html, "flv4" + "\":\"(.*?)\""));
        for (int i = 0; i < result.size();) {
            if (StringUtils.isEmpty(result.get(i))) {
                result.remove(i);
            } else {
                i++;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.print(getLiveUrl("cctv1:hls4"));
    }
}

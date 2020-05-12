package com.lcjian.spunsugar.parser;

import java.util.ArrayList;
import java.util.List;

import com.lcjian.spunsugar.util.StringUtils;

public class CMVideoHandler {

    public static List<String> getLiveUrl(String html) {
        List<String> result = new ArrayList<String>();
        String url = Common.r1(html, "url" + "\":\"(.*?)\"");
        if (url != null) {
            url = url.trim().replace("\\", "");
        }
        if (!StringUtils.isEmpty(url)) {
            result.add(url);
        }
        return result;
    }
}

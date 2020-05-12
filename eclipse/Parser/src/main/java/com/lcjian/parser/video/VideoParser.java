package com.lcjian.parser.video;

import com.lcjian.util.StringUtils;

public class VideoParser {

    public String parse(String type, String url) {
        if ("bilibili".equals(type)) {
            String playUrl = new BiliBiliParser().parse(url);
            if (StringUtils.isEmpty(playUrl)) {
                return new BiliBiliH5Parser().parse(url);
            } else {
                return playUrl;
            }
        } else if ("letv".equals(type)) {
            return new LeTVParser().parse(url);
        } else if ("qianmo".equals(type)) {
            return new QianmoParser().parse(url);
        } else if ("87fuli".equals(type) || "zxfuli".equals(type)) {
            return new BQfuliParser().parse(url);
        }
        return null;
    }

}

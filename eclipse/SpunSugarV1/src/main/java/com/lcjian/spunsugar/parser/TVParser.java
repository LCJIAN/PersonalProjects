package com.lcjian.spunsugar.parser;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;

public class TVParser {

    public String getLiveUrl(String key, String web) {
        String liveUrl = null;
        String liveKey = null;
        try {
            liveKey = URLDecoder.decode(key, "utf-8");
            switch (web) {
            case ("cntv"):
                liveUrl = CntvHandler.getLiveUrl(liveKey);
                break;
            case ("ahtv"):
                liveUrl = AhtvHandler.getLiveUrl(liveKey);
                break;
            case ("ifeng"):
                liveUrl = IFengHandler.getLiveUrl(liveKey);
                break;
            case ("letv"):
                liveUrl = LetvHandler.getLiveUrl(liveKey);
                break;
            case ("pptv"):
                liveUrl = PPTVHandler.getLiveUrl(liveKey);
                break;
            case ("qq"):
                liveUrl = QQHandler.getLiveUrl(liveKey);
                break;
            case ("sohu"):
                liveUrl = SohuHandler.getLiveUrl(liveKey);
                break;
            case ("wasu"):
                liveUrl = WasuHandler.getLiveUrl(liveKey);
                break;
            default:
                liveUrl = StaticHandler.getLiveUrl(liveKey);
                break;
            }
            if ((liveUrl == null) || (liveUrl.equals("")))
                liveUrl = "http://202.38.73.228/error";
        } catch (Exception e) {
            e.printStackTrace();
            liveUrl = "http://202.38.73.228/error";
        }
        return liveUrl;
    }

    public List<String> getLiveUrl2(String data, String type) {
        List<String> liveUrls = null;
        try {
            switch (type) {
            case ("cntv"):
                liveUrls = CntvHandler.getLiveUrl2(data);
                break;
            case ("cmvideo"):
                liveUrls = CMVideoHandler.getLiveUrl(data);
                break;
            default:
                liveUrls = Collections.emptyList();
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return liveUrls;
    }
}

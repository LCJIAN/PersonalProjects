package com.lcjian.spunsugar.crawlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.lcjian.spunsugar.entity.TvLiveSource;
import com.lcjian.spunsugar.entity.TvStation;
import com.lcjian.spunsugar.service.TvStationService;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;

@Crawler(name = "TvCrawler", httpType = SeimiHttpType.OK_HTTP3, useCookie = true, httpTimeOut = 60000)
public class TvCrawler extends BaseSeimiCrawler {

    @Autowired
    private TvStationService tvStationService;
    
    @Override
    protected void push(Request request) {
        request.setMaxReqCount(20);
        request.setSkipDuplicateFilter(true);
        super.push(request);
    }

    @Override
    public String getUserAgent() {
        return "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.23 Mobile Safari/537.36";
    }

    @Override
    public String[] startUrls() {
        return new String[] { "http://www.hlyy.cc/zxtv/m/" };
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void callByCron() {
        if (queue.len("TvCrawler") == 0) {
            push(Request.build("http://www.hlyy.cc/zxtv/m/", "start").setSkipDuplicateFilter(true));
        }
    }

    @Override
    public void start(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> tvGroups = doc.sel("//div[@class='panel panel-default']");
            List<Object> a = new ArrayList<>();
            for (Object tvGroup : tvGroups) {
                JXDocument tempDoc = new JXDocument(tvGroup.toString());
                String type = tempDoc.sel("//div[@class='panel-heading']/text()").get(0).toString();
                List<Object> tvList = tempDoc.sel("//td/a");
                for (Object tv : tvList) {
                    JXDocument tvDoc = new JXDocument(tv.toString());
                    String tvHref = tvDoc.sel("//a/@href").get(0).toString();
                    String tvName = tvDoc.sel("//a/text()").get(0).toString();
                    Map<String, String> meta = new HashMap<String, String>();
                    meta.put("tv_name", tvName);
                    meta.put("tv_type", type);
                    push(new Request(processHref(tvHref), "getSource").setMeta(meta));
                    a.add(tv);
                }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    public void getSource(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> tvSources = doc.sel("//label[@class='channel-name2']/a");
            TvStation tvStation = new TvStation();
            tvStation.setName(response.getMeta().get("tv_name"));
            tvStation.setChannel(response.getMeta().get("tv_name"));
            tvStation.setType(response.getMeta().get("tv_type"));
            tvStation.setTvLiveSources(tvSources.stream()
                    .map(o -> new JXDocument(o.toString()))
                    .map(d -> {
                        TvLiveSource tvSource = new TvLiveSource();
                        try {
                            String tvSourceHref = d.sel("//a/@href").get(0).toString();
                            String tvSourceName = d.sel("//a/text()").get(0).toString();
                            if (StringUtils.isEmpty(tvSourceName)) {
                                tvSourceName = d.sel("//a/font/text()").get(0).toString();
                            }
                            tvSource.setSite(tvSourceName);
                            tvSource.setUrl(processHref(tvSourceHref));
                        } catch (Exception e) {
                            logger.info(e.getMessage());
                        }
                        return tvSource;
                    })
                    .collect(Collectors.toSet()));
            
            TvLiveSource s = new TvLiveSource();
            s.setSite("默认");
            s.setUrl(response.getUrl());
            tvStation.getTvLiveSources().add(s);
            
            tvStationService.create(tvStation);
        } catch (Exception e) {
          logger.info(e.getMessage());
        }
    }

    private static String processHref(String href) {
        if (StringUtils.isEmpty(href)) {
            return href;
        } else {
            if (!StringUtils.startsWith(href, "http")) {
                href = "http://www.hlyy.cc/zxtv/m/" + href;
            }
            return href;
        }
    }
}

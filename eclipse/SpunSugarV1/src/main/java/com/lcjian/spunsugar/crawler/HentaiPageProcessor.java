package com.lcjian.spunsugar.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import com.google.gson.Gson;
import com.lcjian.spunsugar.entity.HentaiAnimeEpisode;
import com.lcjian.spunsugar.entity.HentaiAnimeGenre;
import com.lcjian.spunsugar.entity.HentaiAnimeMaker;
import com.lcjian.spunsugar.entity.HentaiAnimeSeries;
import com.lcjian.spunsugar.util.DateUtils;

public class HentaiPageProcessor implements PageProcessor {
    
    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,ja;q=0.6,en;q=0.4")
            .addHeader("Accept-Encoding", "gzip, deflate, sdch")
            .addHeader("Cache-Control", "max-age=0")
            .setSleepTime(1000);

    @Override
    // process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
    public void process(Page page) {
        // 部分二：定义如何抽取页面信息，并保存下来
        String url = page.getUrl().toString();
        if (url.equals("http://www.dmm.co.jp/digital/anime/-/genre/")) {
            List<String> typeGroups = page.getHtml().xpath("//div[@class='d-area area-list']").all();
            List<HentaiAnimeGenre> hentaiAnimeGenres = new ArrayList<HentaiAnimeGenre>();
            for (String typeGroup : typeGroups) {
                Html typeGroupHtml = new Html(typeGroup);
                String genreType = typeGroupHtml.xpath("//div[@class='d-capt']/text()").toString();
                if (!genreType.trim().equals("おすすめジャンル")) {
                    List<String> genres = typeGroupHtml.xpath("//div[@class='d-sect']/ul[@class='d-item d-boxtbllist d-4col']/li/a").all();
                    for (String genre : genres) {
                        Html genreHtml = new Html(genre);
                        String link = genreHtml.xpath("//a/@href").toString();
                        HentaiAnimeGenre hentaiAnimeGenre = new HentaiAnimeGenre();
                        hentaiAnimeGenre.setId(Integer.parseInt(link.substring(link.indexOf("id=") + 3, link.length() - 1)));
                        hentaiAnimeGenre.setName(genreHtml.xpath("//a/text()").toString());
                        hentaiAnimeGenre.setType(genreType);
                        hentaiAnimeGenres.add(hentaiAnimeGenre);
                    }
                }
            }
            page.putField("hentai_anime_genres", hentaiAnimeGenres);
            page.addTargetRequest("http://www.dmm.co.jp/digital/anime/-/maker/");
        } else if (url.equals("http://www.dmm.co.jp/digital/anime/-/maker/")) {
            List<String> makerTrs = page.getHtml().xpath("//table[@cellspacing='0']/tbody/tr").all();
            List<String> makerTds = new ArrayList<String>();
            List<HentaiAnimeMaker> hentaiAnimeMakers = new ArrayList<HentaiAnimeMaker>();
            for (String makerTr : makerTrs) {
                Html makerTrHtml = new Html("<table>" + makerTr + "</table>");
                makerTds.addAll(makerTrHtml.xpath("//td").all());
            }
            for (String makerTd : makerTds) {
                Html makerTdHtml = new Html(makerTd);
                String link = makerTdHtml.xpath("//p/a/@href").toString();
                HentaiAnimeMaker hentaiAnimeMaker = new HentaiAnimeMaker();
                hentaiAnimeMaker.setId(Integer.parseInt(link.substring(link.indexOf("id=") + 3, link.length() - 1)));
                hentaiAnimeMaker.setLogo(makerTdHtml.xpath("//p/a/img/@src").toString());
                hentaiAnimeMaker.setName(makerTdHtml.xpath("//p/a/text()").toString());
                hentaiAnimeMaker.setDescription(makerTdHtml.xpath("//div/text()").toString());
                hentaiAnimeMakers.add(hentaiAnimeMaker);
            }
            page.putField("hentai_anime_makers", hentaiAnimeMakers);
            page.addTargetRequest("http://www.dmm.co.jp/digital/anime/-/series/=/sort=ranking/");
        } else if (url.startsWith("http://www.dmm.co.jp/digital/anime/-/series/=/sort=ranking/")) {
            List<String> seriesTables = page.getHtml().xpath("//table[@class='w100']").all();
            List<String> seriesTds = new ArrayList<String>();
            List<HentaiAnimeSeries> hentaiAnimeSeries = new ArrayList<HentaiAnimeSeries>();
            for (String seriesTable : seriesTables) {
                Html seriesTableHtml = new Html(seriesTable);
                String seriesTr = seriesTableHtml.xpath("//tr").all().get(0);
                Html seriesTrHtml = new Html("<table>" + seriesTr + "</table>");
                seriesTds.addAll(seriesTrHtml.xpath("//td").all().subList(0, 2));
            }
            for (String seriesTd : seriesTds) {
                Html seriesTdHtml = new Html(seriesTd);
                String link = seriesTdHtml.xpath("//a/@href").all().get(0);
                HentaiAnimeSeries item = new HentaiAnimeSeries();
                item.setId(Integer.parseInt(link.substring(link.indexOf("id=") + 3, link.length() - 1)));
                item.setName(seriesTdHtml.xpath("//a/img/@alt").toString());
                item.setOverview(seriesTdHtml.xpath("//div[@class='tx-work mg-b12 left']/text()").toString());
                item.setPoster(seriesTdHtml.xpath("//a/img/@src").toString());
                hentaiAnimeSeries.add(item);
            }
            page.putField("hentai_anime_series", hentaiAnimeSeries);
            String paginationControl = page.getHtml().xpath("//div[@class='paginationControl']").all().get(0);
            Html paginationControlHtml = new Html(paginationControl);
            List<String> links = paginationControlHtml.xpath("//a/@href").all();
            String lastLink = links.get(links.size() - 1);
            int pageNum = Integer.parseInt(paginationControlHtml.xpath("//b/text()").all().get(0).trim());
            if (pageNum < Integer.parseInt(lastLink.substring(lastLink.indexOf("page=") + 5, lastLink.length() - 1))) {
                page.addTargetRequest(lastLink);
            } else {
                page.addTargetRequest("http://www.dmm.co.jp/digital/anime/-/list/=/limit=120/sort=date/");
            }
        } else if (url.startsWith("http://www.dmm.co.jp/digital/anime/-/list/=/limit=120/sort=date/")) {
            List<String> episodeDivs = page.getHtml().xpath("//ul[@id='list']/li/div").all();
            List<HentaiAnimeEpisode> hentaiAnimeEpisodes = new ArrayList<HentaiAnimeEpisode>();
            for(String episodeDiv : episodeDivs) {
                Html episodeDivHtml = new Html(episodeDiv);
                HentaiAnimeEpisode item = new HentaiAnimeEpisode();
                String link = episodeDivHtml.xpath("//p[@class='tmb']/a/@href").toString();
                String posterSmall = episodeDivHtml.xpath("//p[@class='tmb']/a/span/img/@src").toString();
                String name = episodeDivHtml.xpath("//p[@class='tmb']/a/span/img/@alt").toString();
                float rating = Float.parseFloat(episodeDivHtml.xpath("//div[@class='value']/p[@class='rate']/span/span/text()").toString().replace("-", "0"));
                item.setId(link.substring(link.indexOf("cid=") + 4, link.length() - 1));
                item.setPosterSmall(posterSmall);
                item.setName(name);
                item.setRating(rating);
                hentaiAnimeEpisodes.add(item);
                page.addTargetRequest(link);
            }
            page.putField("hentai_anime_episodes", hentaiAnimeEpisodes);
            
            String boxcaptside = page.getHtml().xpath("//div[@class='list-boxcaptside list-boxpagenation']").all().get(0);
            Html boxcaptsideHtml = new Html(boxcaptside);
            List<String> links = boxcaptsideHtml.xpath("//ul/li/a").all();
            for (String link : links) {
                if (link.contains("次へ")) {
                    Html linkHtml = new Html(link);
                    page.addTargetRequest(linkHtml.xpath("//a/@href").toString());
                }
            }
        } else {
            List<Selectable> trs = page.getHtml().xpath("//table[@class='mg-b20']/tbody/tr").nodes();
            HentaiAnimeEpisode item = new HentaiAnimeEpisode();
            for (Selectable tr : trs) {
                List<Selectable> tds = tr.xpath("//td/text()").nodes();
                if (tds == null || tds.size() < 2) {
                    continue;
                }
                String key = tds.get(0).toString();
                String value = tds.get(1).toString();
                if (key.contains("配信開始日")) {
                    item.setDeliveryStartDate(DateUtils.convertStrToDate(value, "yyyy/MM/dd"));
                }
                if (key.contains("収録時間：")) {
                    item.setDuration(Integer.valueOf(value.substring(0, value.indexOf("分"))));
                }
                if (key.contains("シリーズ：")) {
                    String link = tr.xpath("//td/a/@href").toString();
                    if (link != null) {
                        HentaiAnimeSeries hentaiAnimeSeries = new HentaiAnimeSeries();
                        hentaiAnimeSeries.setId(Integer.parseInt(link.substring(link.indexOf("id=") + 3, link.length() - 1)));
                        item.setHentaiAnimeSeries(hentaiAnimeSeries);
                    }
                }
                if (key.contains("メーカー：")) {
                    String link = tr.xpath("//td/a/@href").toString();
                    if (link != null) {
                        HentaiAnimeMaker hentaiAnimeMaker = new HentaiAnimeMaker();
                        hentaiAnimeMaker.setId(Integer.parseInt(link.substring(link.indexOf("id=") + 3, link.length() - 1)));
                        item.setHentaiAnimeMaker(hentaiAnimeMaker);
                    }
                }
                if (key.contains("ジャンル：")) {
                    List<Selectable> genreLinks = tr.xpath("//td/a").nodes();
                    Set<HentaiAnimeGenre> hentaiAnimeGenres = new HashSet<HentaiAnimeGenre>();
                    for (Selectable genreLink : genreLinks) {
                        String link = genreLink.xpath("//a/@href").toString();
                        if (link != null) {
                            HentaiAnimeGenre hentaiAnimeGenre = new HentaiAnimeGenre();
                            hentaiAnimeGenre.setId(Integer.parseInt(link.substring(link.indexOf("id=") + 3, link.length() - 1)));
                            hentaiAnimeGenres.add(hentaiAnimeGenre);
                        }
                    }
                    item.setHentaiAnimeGenres(hentaiAnimeGenres);
                }
            }
            item.setId(url.substring(url.indexOf("cid=") + 4, url.length() - 1));
            item.setPosterLarge(page.getHtml().xpath("//div[@id='sample-video']/a/@href").toString());
            item.setPosterMedium(page.getHtml().xpath("//div[@id='sample-video']/a/img/@src").toString());
            item.setOverview(page.getHtml().xpath("//div[@class='mg-b20 lh4']/text()").toString());
            List<String> sampleImages = page.getHtml().xpath("//div[@id='sample-image-block']/a/img/@src").all();
            List<HashMap<String, String>> sampleImageGroups = new ArrayList<HashMap<String, String>>();
            for (String sampleImage : sampleImages) {
                HashMap<String, String> itemImage = new HashMap<String, String>();
                itemImage.put("small", sampleImage);
                itemImage.put("large", sampleImage.replace("-", "jp-"));
                sampleImageGroups.add(itemImage);
            }
            item.setSampleImage(new Gson().toJson(sampleImageGroups));
            page.putField("hentai_anime_episode_detail", item);
        }
    }

    @Override
    public Site getSite() {
        List<String[]> httpProxyList = new ArrayList<String[]>();
        httpProxyList.add(new String[] { "127.0.0.1", "56429" });
        site.setHttpProxyPool(httpProxyList);
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new HentaiPageProcessor())
                .addUrl("http://www.dmm.co.jp/digital/anime/-/genre/")
                .addPipeline(new ConsolePipeline())
                .addPipeline(new HentaiPipeline())
                .thread(20)
                .run(); // 启动爬虫
    }
}
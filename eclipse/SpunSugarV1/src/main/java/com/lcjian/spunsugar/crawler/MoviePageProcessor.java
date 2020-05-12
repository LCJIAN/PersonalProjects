package com.lcjian.spunsugar.crawler;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import com.google.gson.Gson;
import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieVideo;
import com.lcjian.spunsugar.util.DateUtils;
import com.lcjian.spunsugar.util.Native2AsciiUtils;
import com.lcjian.spunsugar.util.StringUtils;

public class MoviePageProcessor implements PageProcessor {
    
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
        if (url.startsWith("http://www.yingshidaquan.cc/vod-show-id-1-p")) {
            List<String> movieUrls = page.getHtml().xpath("//ul[@class='mlist']/li/a/@href").all();
            List<String> nextPage = page.getHtml().xpath("//div[@id='pages']/a[@class='next pagegbk']/@href").all();
            page.addTargetRequests(movieUrls);
            if (nextPage != null && !nextPage.isEmpty()) {
                page.addTargetRequest(nextPage.get(0));
            }
        } else if (url.startsWith("http://www.yingshidaquan.cc/html")) {
            if (page.getHtml().xpath("//div[@class='playhd-list']/a/@href").get() != null) {
                page.addTargetRequest(page.getHtml().xpath("//div[@class='playhd-list']/a/@href").get());
            } else if (page.getHtml().xpath("//div[@class='playdvd-list']/a/@href").get() != null) {
                page.addTargetRequest(page.getHtml().xpath("//div[@class='playdvd-list']/a/@href").get());
            } else if (page.getHtml().xpath("//div[@class='play-list']/a/@href").get() != null) {
                page.addTargetRequest(page.getHtml().xpath("//div[@class='play-list']/a/@href").get());
            } else {
                return;
            }
            Date date = null;
            try {
                date = DateUtils.convertStrToDate(page.getHtml().xpath("//div[@class='info']/ul/li/text()").all().get(0).substring(0, 4), "yyyy");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Movie movie = new Movie();
            try {
                movie.setOverview(page.getHtml().xpath("//div[@class='endtext']/text()").get());
                movie.setPoster(page.getHtml().xpath("//div[@class='pic']/img/@src").get());
                movie.setReleaseDate(date);
                movie.setTitle(page.getHtml().xpath("//div[@class='info']/h1/text()").get().replace("在线播放", ""));
                movie.setVoteAverage(Float.parseFloat(page.getHtml().xpath("//span[@class='Goldnum']/text()").get()));
                movie.setCrawlerId(url.replace("http://www.yingshidaquan.cc/html/", "").replace(".html", ""));
                movie.setCreateTime(new Timestamp(System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (movie.getReleaseDate() != null) {
                String doubanId = MdbIdGetter.getDoubanId(DateUtils.convertDateToStr(movie.getReleaseDate(), "yyyy"), movie.getTitle());
                movie.setDoubanId(doubanId);
                if (!StringUtils.isEmpty(doubanId)) {
                    String imdbId = MdbIdGetter.getImdbId(doubanId);
                    movie.setImdbId(imdbId);
                    if (!StringUtils.isEmpty(imdbId)) {
                        String tmdbId = MdbIdGetter.getTmdbId(imdbId);
                        movie.setTmdbId(tmdbId);
                    }
                }
            }
            page.putField("movie", movie);
        } else {
            Html html = new Html(Native2AsciiUtils.ascii2Native(page.getHtml().get()));
            String json = html.xpath("//div[@class='fl']/script").all().get(0)
                    .replace("<script language=\"javascript\">var ff_urls='", "").replace("';</script>", "");
            Root root = (new Gson()).fromJson(json, Root.class);
            String crawlerId = url.replace("http://www.yingshidaquan.cc/play/", "");
            crawlerId = crawlerId.substring(0, crawlerId.indexOf("-"));
            Movie movie = new Movie();
            movie.setCrawlerId(crawlerId);
            List<Data> datas = root.getData();
            
            List<MovieVideo> videos = new ArrayList<MovieVideo>();
            for (Data data : datas) {
                String type = data.getPlayname();
                List<List<String>> urls = data.getPlayurls();
                for (List<String> urlPlay : urls) {
                    if (urlPlay.get(0).contains("点播")
                            || type.equals("bilibili")
                            || type.equals("acfun")
                            || type.equals("letv")
                            || type.equals("56")
                            || type.equals("pptv")
                            || type.equals("yuku")
                            || type.equals("sohu")
                            || type.equals("qq")
                            || type.equals("tudo")
                            || type.equals("mediahd")
                            || type.equals("cool")
                            || type.equals("weiyun")
                            || type.equals("imgo")
                            || type.equals("qianmo")
                            || type.equals("superm3u8")
                            || type.equals("superurl")) {
                        MovieVideo video = new MovieVideo();
                        video.setMovie(movie);
                        video.setType(type);
                        video.setName(urlPlay.get(0));
                        video.setUrl(urlPlay.get(1));
                        videos.add(video);
                    }
                }
            }
            page.putField("videos", videos);
        }
    }
    
    @Override
    public Site getSite() {
//        List<String[]> httpProxyList = new ArrayList<String[]>();
//        httpProxyList.add(new String[] { "127.0.0.1", "55123" });
//        site.setHttpProxyPool(httpProxyList);
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new MoviePageProcessor())
                .addUrl("http://www.yingshidaquan.cc/vod-show-id-1-p.html")
                .addPipeline(new ConsolePipeline())
                .addPipeline(new MoviePipeline())
                .thread(1)
                .run(); // 启动爬虫
    }
    
    public class Data {
        private String servername;

        private String playname;

        private List<List<String>> playurls;

        public void setServername(String servername) {
            this.servername = servername;
        }

        public String getServername() {
            return this.servername;
        }

        public void setPlayname(String playname) {
            this.playname = playname;
        }

        public String getPlayname() {
            return this.playname;
        }

        public void setPlayurls(List<List<String>> playurls) {
            this.playurls = playurls;
        }

        public List<List<String>> getPlayurls() {
            return this.playurls;
        }
    }

    public class Root {
        private List<String> Vod;

        private List<Data> Data;

        public void setVod(List<String> Vod) {
            this.Vod = Vod;
        }

        public List<String> getVod() {
            return this.Vod;
        }

        public void setData(List<Data> Data) {
            this.Data = Data;
        }

        public List<Data> getData() {
            return this.Data;
        }
    }
}
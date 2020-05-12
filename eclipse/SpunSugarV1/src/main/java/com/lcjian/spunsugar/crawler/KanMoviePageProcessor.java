package com.lcjian.spunsugar.crawler;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieVideo;
import com.lcjian.spunsugar.util.DateUtils;
import com.lcjian.spunsugar.util.StringUtils;

public class KanMoviePageProcessor implements PageProcessor {
    
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
        if (url.startsWith("http://kan.sogou.com/dianying/----")) {
            page.addTargetRequests(page.getHtml().xpath("//div[@class='cell cf']/a/@href").all());
            page.addTargetRequest(page.getHtml().xpath("//a[@class='next']/@href").get());
        } else {
            Movie movie = new Movie();
            movie.setCreateTime(new Timestamp(System.currentTimeMillis()));
            
            String doubanId = page.getHtml().xpath("//div[@class='dbc']/a/@href").get().replace("http://movie.douban.com/subject/", "");
            float voteAverage = Float.parseFloat(page.getHtml().xpath("//div[@class='dbc']/a/i/text()").get());
            String title = page.getHtml().xpath("//h1[@class='title txt-overflow']/a/text()").get();
            String overview = page.getHtml().xpath("//div[@id='tv_summary']/span/text()").get();
            List<String> strs = page.getHtml().xpath("//span[@class='width-250']/a/text()").all();
            for (String str : strs) {
                if (StringUtils.isNumeric(str)) {
                    movie.setReleaseDate(DateUtils.convertStrToDate(str, "yyyy"));
                    break;
                }
            }
            movie.setOverview(overview);
            movie.setPoster(page.getHtml().xpath("//dl[@class='comic-intro movie-intro tv-intro cf']/dt/a/img/@src").get());
            movie.setTitle(title);
            movie.setVoteAverage(voteAverage);
            movie.setCrawlerId(url.replace("http://kan.sogou.com/player/", "").replace("/", ""));
            movie.setDoubanId(doubanId);
            if (!StringUtils.isEmpty(doubanId)) {
                String imdbId = MdbIdGetter.getImdbId(doubanId);
                movie.setImdbId(imdbId);
                if (!StringUtils.isEmpty(imdbId)) {
                    String tmdbId = MdbIdGetter.getTmdbId(imdbId);
                    movie.setTmdbId(tmdbId);
                }
            }
            
            Set<MovieVideo> videos = new HashSet<MovieVideo>();
            List<String> plays = page.getHtml().xpath("//div[@class='from2 cf']/div/a/@href").all();
            List<String> types = page.getHtml().xpath("//div[@class='from2 cf']/div/a/text()").all();
            for (int i = 0; i < plays.size(); i++) {
                MovieVideo video = new MovieVideo();
                video.setType(types.get(i));
                video.setUrl(plays.get(i));
                video.setMovie(movie);
                videos.add(video);
            }
            movie.setMovieVideos(videos);
            page.putField("movie", movie);
        }
    }

    @Override
    public Site getSite() {
//        List<String[]> httpProxyList = new ArrayList<String[]>();
//        httpProxyList.add(new String[] { "127.0.0.1", "56429" });
//        site.setHttpProxyPool(httpProxyList);
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new KanMoviePageProcessor())
                .addUrl("http://kan.sogou.com/dianying/----/")
                .addPipeline(new KanMoviePipeline())
                .addPipeline(new ConsolePipeline())
                .thread(1)
                .run(); // 启动爬虫
    }
}
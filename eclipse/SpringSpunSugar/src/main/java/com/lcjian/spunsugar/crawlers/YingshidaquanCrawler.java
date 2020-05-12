package com.lcjian.spunsugar.crawlers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieVideo;
import com.lcjian.spunsugar.service.MovieService;
import com.lcjian.spunsugar.util.MdbIdGetter;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;

@Crawler(name = "YingshidaquanCrawler")
public class YingshidaquanCrawler extends BaseSeimiCrawler {

    @Autowired
    private MovieService movieService;

    @Override
    public String getUserAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2700.0 Safari/537.36";
    }

    @Override
    public String[] startUrls() {
        return new String[] { "http://www.yingshidaquan.cc/vod-show-id-1-p.html" };
    }

    @Override
    public void start(Response response) {
        push(new Request("http://www.yingshidaquan.cc/vod-show-id-1-p-100.html", "getPrePageAndMovie"));
    }

    public void getPrePageAndMovie(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> links = doc.sel("//a");
            for (Object link : links) {
                if (link.toString().contains("上一页")) {
                    JXDocument tempDoc = new JXDocument(link.toString());
                    String prePageUrl = "http://www.yingshidaquan.cc" + tempDoc.sel("//a/@href").get(0);
                    logger.info("prePageUrl: {}", prePageUrl);
                    push(new Request(prePageUrl, "getPrePageAndMovie"));
                    break;
                }
            }
            List<Object> movieUrls = doc.sel("//ul[@class='mlist']/li/a/@href");
            logger.info("movieUrls: {}", movieUrls);
            for (Object s : movieUrls) {
                push(new Request("http://www.yingshidaquan.cc" + s.toString(), "getMovieDetail"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMovieDetail(Response response) {
        JXDocument doc = response.document();
        try {
            String video;
            if (!doc.sel("//div[@class='playhd-list']/a/@href").isEmpty()) {
                video = doc.sel("//div[@class='playhd-list']/a/@href").get(0).toString();
            } else if (!doc.sel("//div[@class='playdvd-list']/a/@href").isEmpty()) {
                video = doc.sel("//div[@class='playdvd-list']/a/@href").get(0).toString();
            } else if (!doc.sel("//div[@class='play-list']/a/@href").isEmpty()) {
                video = doc.sel("//div[@class='play-list']/a/@href").get(0).toString();
            } else {
                return;
            }
            String overview    = doc.sel("//div[@class='endtext']/text()").get(0).toString();
            String poster      = doc.sel("//div[@class='pic']/img/@src").get(0).toString();
            String releaseDate = doc.sel("//div[@class='info']/ul/li/text()").get(0).toString();
            String title       = doc.sel("//div[@class='info']/h1/text()").get(0).toString();
            String voteAverage = doc.sel("//span[@class='Goldnum']/text()").get(0).toString();

            Movie movie = new Movie();
            movie.setOverview(overview);
            movie.setPoster(poster);
            movie.setReleaseDate(LocalDate.parse(releaseDate.replace("全集", "").replace(" ", "") + "/01/01", DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            movie.setTitle(title);
            movie.setVoteAverage(Float.parseFloat(voteAverage));
            movie.setCrawlerId(response.getRealUrl());
            movie.setCreateTime(LocalDateTime.now());

            if (movie.getReleaseDate() != null) {
                String doubanId = MdbIdGetter.getDoubanId(movie.getReleaseDate().getYear(), movie.getTitle(), "movie");
                movie.setDoubanId(doubanId);
                if (!StringUtils.isEmpty(doubanId)) {
                    String imdbId = MdbIdGetter.getImdbId(doubanId);
                    movie.setImdbId(imdbId);
                    if (!StringUtils.isEmpty(imdbId)) {
                        String tmdbId = MdbIdGetter.getTmdbId(imdbId, "movie");
                        movie.setTmdbId(tmdbId);
                        MdbIdGetter.setTmdbInfo(movie);
                    }
                }
            }
            if (movie.getTitle().contains("/")) {
                movie.setTitle(movie.getTitle().split("/")[0]);
            }

            Set<MovieVideo> movieVideos = new HashSet<MovieVideo>();
            MovieVideo movieVideo = new MovieVideo();
            movieVideo.setUrl("http://www.yingshidaquan.cc" + video);
            movieVideo.setSite("yingshidaquan");
            movieVideo.setName("影视大全");
            movieVideos.add(movieVideo);
            movie.setMovieVideos(movieVideos);
            logger.info("movie: {}", movie);
            movieService.create(movie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

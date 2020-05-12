package com.lcjian.spunsugar.crawlers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.MovieGenre;
import com.lcjian.spunsugar.entity.MovieProductionCountry;
import com.lcjian.spunsugar.entity.MovieVideo;
import com.lcjian.spunsugar.entity.Recommend;
import com.lcjian.spunsugar.entity.TvShow;
import com.lcjian.spunsugar.entity.TvShowGenre;
import com.lcjian.spunsugar.entity.TvShowProductionCountry;
import com.lcjian.spunsugar.entity.TvShowVideo;
import com.lcjian.spunsugar.service.MovieService;
import com.lcjian.spunsugar.service.RecommendService;
import com.lcjian.spunsugar.service.TvShowService;
import com.lcjian.spunsugar.util.MdbIdGetter;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;

@Crawler(name = "BQfuliCrawler", httpType = SeimiHttpType.OK_HTTP3, useCookie = true)
public class BQfuliCrawler extends BaseSeimiCrawler {

    @Autowired
    private MovieService movieService;
    
    @Autowired
    private TvShowService tvShowService;
    
    @Autowired
    private RecommendService recommendService;
    
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
        return new String[] { "http://www.11wa.com",
                              "http://www.11wa.com/type/1/1.html",
                              "http://www.11wa.com/type/2/1.html",
                              "http://www.11wa.com/type/9/1.html" };
    }
    
    @Scheduled(cron = "0 0 1 * * ?")
    public void callByCron() {
        if (queue.len("BQfuliCrawler") == 0) {
            push(Request.build("http://www.11wa.com", "start").setSkipDuplicateFilter(true));
            push(Request.build("http://www.11wa.com/type/1/5.html", "getPrePageAndSubject"));
            push(Request.build("http://www.11wa.com/type/2/5.html", "getPrePageAndSubject"));
            push(Request.build("http://www.11wa.com/type/9/5.html", "getPrePageAndSubject"));
        }
    }

    @Override
    public void start(Response response) {
        JXDocument doc = response.document();
        try {
            if (StringUtils.equals(response.getRequest().getUrl(), "http://www.11wa.com")) {
                List<Object> bannerLnks = doc.sel("//ul[@class='focusList']/li/a");
                for (Object link : bannerLnks) {
                    JXDocument tempDoc = new JXDocument(link.toString());
                    String subjectUrl = "http://www.11wa.com" + tempDoc.sel("//a/@href").get(0);
                    String extra = tempDoc.sel("//a/span/em/text()").get(0).toString();
                    String extra1 = tempDoc.sel("//a/img/@data-src").get(0).toString();
                    Map<String, String> params = new HashMap<>();
                    params.put("recommend_title", "banner");
                    params.put("recommend_extra", extra);
                    params.put("recommend_extra1", extra1);
                    push(new Request(subjectUrl, "getSubjectDetail").setMeta(params));
                }
                List<Object> recentLnks = doc.sel("//li[@class='p1 m1']/a");
                for (Object link : recentLnks) {
                    JXDocument tempDoc = new JXDocument(link.toString());
                    String subjectUrl = "http://www.11wa.com" + tempDoc.sel("//a/@href").get(0);
                    Map<String, String> params = new HashMap<>();
                    params.put("recommend_title", "recent");
                    push(new Request(subjectUrl, "getSubjectDetail").setMeta(params));
                }
            } else {
                List<Object> pages = doc.sel("//div[@id='page']/ul/li/span/text()");
                for (Object page : pages) {
                    Pattern pattern = Pattern.compile("([0-9]{1,})页");
                    Matcher matcher = pattern.matcher(page.toString());
                    if (matcher.find()) {
                        String lastPageUrl = response.getRealUrl().replaceFirst("1.html", matcher.group(1) + ".html");
                        lastPageUrl = response.getRealUrl().replaceFirst("1.html", "2.html");
                        logger.info("lastPageUrl: {}", lastPageUrl);
                        push(new Request(lastPageUrl, "getPrePageAndSubject"));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPrePageAndSubject(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> links = doc.sel("//a");
            for (Object link : links) {
                if (link.toString().contains("上一页")) {
                    JXDocument tempDoc = new JXDocument(link.toString());
                    String prePageUrl = "http://www.11wa.com" + tempDoc.sel("//a/@href").get(0);
                    logger.info("prePageUrl: {}", prePageUrl);
                    push(new Request(prePageUrl, "getPrePageAndSubject"));
                    break;
                }
            }
            List<Object> subjectUrls = doc.sel("//li[@class='p1 m1']/a/@href");
            logger.info("subjectUrls: {}", subjectUrls);
            for (Object s : subjectUrls) {
                push(new Request(s.toString(), "getSubjectDetail"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSubjectDetail(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> types = doc.sel("//ul[@class='top-nav']/li/a[@class='on']/text()");
            String type        = types == null || types.isEmpty() ? "" : types.get(0).toString();
            String poster      = doc.sel("//div[@class='ct-l']/img[@class='lazy']/@src").get(0).toString();
            String title       = doc.sel("//div[@class='ct-c']/dl/dt[@class='name']/text()").get(0).toString();
            String mainActor   = doc.sel("//div[@class='ct-c']/dl/dt/text()").get(1).toString();
            String genres      = doc.sel("//div[@class='ct-c']/dl/dt/text()").get(2).toString();
            String director    = doc.sel("//div[@class='ct-c']/dl/dd/text()").get(0).toString();
            String country     = doc.sel("//div[@class='ct-c']/dl/dd/text()").get(1).toString();
            String releaseDate = doc.sel("//div[@class='ct-c']/dl/dd/text()").get(2).toString();
            String minutes     = doc.sel("//div[@class='ct-c']/dl/dd/text()").get(3).toString();
            String overview    = doc.sel("//div[@class='tab-jq ctc']/text()").get(0).toString();
            List<Object> videos= doc.sel("//div[@class='show_player_gogo']/ul/li/a");
            String voteAverage = "0";

            if (StringUtils.equals("电影", type) || StringUtils.equals("恐怖", type)) {
                Movie movie = new Movie();
                movie.setTitle(title);
                movie.setPoster(poster);
                movie.setMainActor(StringUtils
                        .join(Arrays.asList(mainActor.split("/"))
                                .stream()
                                .map(s -> s.trim())
                                .collect(Collectors.toList()), ","));
                movie.setDirector(StringUtils
                        .join(Arrays.asList(director.split("/"))
                                .stream()
                                .map(s -> s.trim())
                                .collect(Collectors.toList()), ","));
                movie.setMovieGenres(Arrays.asList(genres.split("/"))
                        .stream()
                        .map(s -> new MovieGenre(s.trim()))
                        .filter(g -> !StringUtils.isEmpty(g.getName()))
                        .collect(Collectors.toSet()));
                movie.setMovieProductionCountries(Arrays.asList(country.split("/"))
                        .stream()
                        .map(s -> {
                            MovieProductionCountry movieProductionCountry = new MovieProductionCountry();
                            movieProductionCountry.setName(s.trim());
                            return movieProductionCountry;
                        })
                        .filter(c -> !StringUtils.isEmpty(c.getName()))
                        .collect(Collectors.toSet()));
                movie.setMovieVideos(videos
                        .stream()
                        .map(v -> {
                            JXDocument tempDoc = new JXDocument(v.toString());
                            MovieVideo movieVideo = new MovieVideo();
                            try {
                                movieVideo.setUrl(tempDoc.sel("//a/@href").get(0).toString());
                                movieVideo.setSite("zxfuli");
                                movieVideo.setName(tempDoc.sel("//a/text()").get(0).toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return movieVideo;
                        })
                        .filter(v -> !StringUtils.isEmpty(v.getUrl()))
                        .collect(Collectors.toSet()));
                movie.setMinutes(Integer.parseInt(minutes.replace("分钟", "")));
                if (releaseDate.contains("/")) {
                    releaseDate = releaseDate.split("/")[0];
                }
                if (!StringUtils.isEmpty(releaseDate)) {
                    Pattern pattern = Pattern.compile("([0-9]{4}-[0-9]{2}-[0-9]{2})");
                    Matcher matcher = pattern.matcher(releaseDate);
                    if (matcher.find()) {
                        releaseDate = matcher.group(1);
                        movie.setReleaseDate(LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    } else {
                        pattern = Pattern.compile("([0-9]{4})");
                        matcher = pattern.matcher(releaseDate);
                        if (matcher.find()) {
                            releaseDate = matcher.group(1) + "-01-01";
                            movie.setReleaseDate(LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        } else {
                            pattern = Pattern.compile("([0-9]{4}-[0-9]{1}-[0-9]{2})");
                            matcher = pattern.matcher(releaseDate);
                            if (matcher.find()) {
                                releaseDate = matcher.group(1);
                                movie.setReleaseDate(LocalDate.parse(releaseDate,
                                        DateTimeFormatter.ofPattern("yyyy-M-dd")));
                            } 
                        }
                    }
                }
                movie.setOverview(overview);
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
                if (StringUtils.isEmpty(movie.getDoubanId())) {
                    logger.warn(title);
                }
                logger.info("movie: {}", movie);
                Movie newMovie = movieService.create(movie);
                
                Map<String, String> meta = response.getMeta();
                if (meta != null && !StringUtils.isEmpty(meta.get("recommend_title"))) {
                    Recommend recommend = new Recommend(meta.get("recommend_title"),
                            "movie", meta.get("recommend_extra"), meta.get("recommend_extra1"), newMovie.getId(), LocalDateTime.now());
                    recommendService.create(recommend);
                }
            } else if (StringUtils.equals("电视", type)) {
                TvShow tvShow = new TvShow();
                tvShow.setTitle(title);
                tvShow.setPoster(poster);
                tvShow.setMainActor(StringUtils
                        .join(Arrays.asList(mainActor.split("/"))
                                .stream()
                                .map(s -> s.trim())
                                .collect(Collectors.toList()), ","));
                tvShow.setDirector(StringUtils
                        .join(Arrays.asList(director.split("/"))
                                .stream()
                                .map(s -> s.trim())
                                .collect(Collectors.toList()), ","));
                tvShow.setTvShowGenres(Arrays.asList(genres.split("/"))
                        .stream()
                        .map(s -> new TvShowGenre(s.trim()))
                        .filter(g -> !StringUtils.isEmpty(g.getName()))
                        .collect(Collectors.toSet()));
                tvShow.setTvShowProductionCountries(Arrays.asList(country.split("/"))
                        .stream()
                        .map(s -> {
                            TvShowProductionCountry tvShowProductionCountry = new TvShowProductionCountry();
                            tvShowProductionCountry.setName(s.trim());
                            return tvShowProductionCountry;
                        })
                        .filter(c -> !StringUtils.isEmpty(c.getName()))
                        .collect(Collectors.toSet()));
                tvShow.setTvShowVideos(videos
                        .stream()
                        .map(v -> {
                            JXDocument tempDoc = new JXDocument(v.toString());
                            TvShowVideo tvShowVideo = new TvShowVideo();
                            try {
                                tvShowVideo.setUrl(tempDoc.sel("//a/@href").get(0).toString());
                                tvShowVideo.setSite("zxfuli");
                                tvShowVideo.setName(tempDoc.sel("//a/text()").get(0).toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return tvShowVideo;
                        })
                        .filter(v -> !StringUtils.isEmpty(v.getUrl()))
                        .collect(Collectors.toSet()));
                tvShow.setMinutes(Integer.parseInt(minutes.replace("分钟", "")));
                if (releaseDate.contains("/")) {
                    releaseDate = releaseDate.split("/")[0];
                }
                if (!StringUtils.isEmpty(releaseDate)) {
                    Pattern pattern = Pattern.compile("([0-9]{4}-[0-9]{2}-[0-9]{2})");
                    Matcher matcher = pattern.matcher(releaseDate);
                    if (matcher.find()) {
                        releaseDate = matcher.group(1);
                        tvShow.setReleaseDate(LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    } else {
                        pattern = Pattern.compile("([0-9]{4})");
                        matcher = pattern.matcher(releaseDate);
                        if (matcher.find()) {
                            releaseDate = matcher.group(1) + "-01-01";
                            tvShow.setReleaseDate(LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                        } else {
                            pattern = Pattern.compile("([0-9]{4}-[0-9]{1}-[0-9]{2})");
                            matcher = pattern.matcher(releaseDate);
                            if (matcher.find()) {
                                releaseDate = matcher.group(1);
                                tvShow.setReleaseDate(LocalDate.parse(releaseDate,
                                        DateTimeFormatter.ofPattern("yyyy-M-dd")));
                            } 
                        }
                    }
                }
                
                tvShow.setOverview(overview);
                tvShow.setVoteAverage(Float.parseFloat(voteAverage));
                tvShow.setCrawlerId(response.getRealUrl());
                tvShow.setCreateTime(LocalDateTime.now());
                
                if (tvShow.getReleaseDate() != null) {
                    String doubanId = MdbIdGetter.getDoubanId(tvShow.getReleaseDate().getYear(), tvShow.getTitle(), "tv");
                    tvShow.setDoubanId(doubanId);
                    if (!StringUtils.isEmpty(doubanId)) {
                        String imdbId = MdbIdGetter.getImdbId(doubanId);
                        tvShow.setImdbId(imdbId);
                        if (!StringUtils.isEmpty(imdbId)) {
                            String tmdbId = MdbIdGetter.getTmdbId(imdbId, "tv");
                            tvShow.setTmdbId(tmdbId);
                            MdbIdGetter.setTmdbInfo(tvShow);
                        }
                    }
                }
                if (StringUtils.isEmpty(tvShow.getDoubanId())) {
                    logger.warn(title);
                }
                logger.info("tvShow: {}", tvShow);
                TvShow newTvShow = tvShowService.create(tvShow);
                
                Map<String, String> meta = response.getMeta();
                if (meta != null && !StringUtils.isEmpty(meta.get("recommend_title"))) {
                    Recommend recommend = new Recommend(meta.get("recommend_title"),
                            "tv_show", meta.get("recommend_extra"), meta.get("recommend_extra1"), newTvShow.getId(), LocalDateTime.now());
                    recommendService.create(recommend);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

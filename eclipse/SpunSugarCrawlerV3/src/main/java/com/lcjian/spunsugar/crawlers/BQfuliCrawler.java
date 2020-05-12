package com.lcjian.spunsugar.crawlers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.lcjian.spunsugar.entity.Genre;
import com.lcjian.spunsugar.entity.Poster;
import com.lcjian.spunsugar.entity.ProductionCountry;
import com.lcjian.spunsugar.entity.Property;
import com.lcjian.spunsugar.entity.Recommend;
import com.lcjian.spunsugar.entity.Subject;
import com.lcjian.spunsugar.entity.Thumbnail;
import com.lcjian.spunsugar.entity.Video;
import com.lcjian.spunsugar.service.RecommendService;
import com.lcjian.spunsugar.service.SubjectService;
import com.lcjian.spunsugar.util.MdbIdGetter;
import com.lcjian.spunsugar.util.Utils;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;

@Crawler(name = "BQfuliCrawler", httpType = SeimiHttpType.OK_HTTP3, useCookie = true, httpTimeOut = 60000)
public class BQfuliCrawler extends BaseSeimiCrawler {

    @Autowired
    private SubjectService subjectService;
    
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
        return new String[] { "http://www.xinju5.com",
                              "http://www.xinju5.com/type/1/1.html",// 电影
                              "http://www.xinju5.com/type/9/1.html",// 恐怖
                              "http://www.xinju5.com/type/2/1.html",// 电视剧
                              "http://www.xinju5.com/type/4/1.html",// 综艺
                              "http://www.xinju5.com/type/7/1.html",// 动漫
                              "http://www.xinju5.com/type/3/1.html",// 福利
                              "http://www.xinju5.com/type/6/1.html",// 音乐
                              };
    }
    
    @Scheduled(cron = "0 0 6 * * ?")
    public void callByCronDay() {
        if (queue.len("BQfuliCrawler") == 0) {
            push(Request.build("http://www.xinju5.com", "start"));
            push(Request.build("http://www.xinju5.com/type/1/4.html", "getPrePageAndSubject"));// 电影
            push(Request.build("http://www.xinju5.com/type/9/4.html", "getPrePageAndSubject"));// 恐怖
            push(Request.build("http://www.xinju5.com/type/2/3.html", "getPrePageAndSubject"));// 电视剧
            push(Request.build("http://www.xinju5.com/type/4/4.html", "getPrePageAndSubject"));// 综艺
            push(Request.build("http://www.xinju5.com/type/7/4.html", "getPrePageAndSubject"));// 动漫
            push(Request.build("http://www.xinju5.com/type/3/4.html", "getPrePageAndSubject"));// 福利
            push(Request.build("http://www.xinju5.com/type/6/4.html", "getPrePageAndSubject"));// 音乐
            
            List<Subject> uncompletedSubjects = subjectService.getUncompletedSubjects();
            for (Subject s : uncompletedSubjects) {
                Map<String, String> meta = new HashMap<>();
                meta.put("ignore_third_info", "true");
                push(new Request(s.getCrawlerId(), "getSubjectDetail").setMeta(meta));
            }
        }
    }
    
    @Scheduled(cron = "0 0 1 1 * ?")
    public void callByCronMonth() {
        push(Request.build("http://www.xinju5.com/type/2/1.html", "start"));// 电视剧
        push(Request.build("http://www.xinju5.com/type/4/1.html", "start"));// 综艺
        push(Request.build("http://www.xinju5.com/type/7/1.html", "start"));// 动漫
    }

    @Override
    public void start(Response response) {
        JXDocument doc = response.document();
        try {
            if (StringUtils.equals(response.getRequest().getUrl(), "http://www.xinju5.com")) {
                List<Object> bannerLnks = doc.sel("//ul[@class='focusList']/li/a");
                for (Object link : bannerLnks) {
                    JXDocument tempDoc = new JXDocument(link.toString());
                    String subjectUrl = processHref(tempDoc.sel("//a/@href").get(0).toString());
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
                    String subjectUrl = processHref(tempDoc.sel("//a/@href").get(0).toString());
                    String other = tempDoc.sel("//a/p[@class='other']/i/text()").get(0).toString();
                    Map<String, String> params = new HashMap<>();
                    params.put("recommend_title", "recent");
                    params.put("other", other);
                    push(new Request(subjectUrl, "getSubjectDetail").setMeta(params));
                }
            } else {
                List<Object> pages = doc.sel("//div[@id='page']/ul/li/span/text()");
                for (Object page : pages) {
                    Pattern pattern = Pattern.compile("([0-9]{1,})页");
                    Matcher matcher = pattern.matcher(page.toString());
                    if (matcher.find()) {
                        String lastPageUrl = response.getRealUrl().replaceFirst("1.html", /*matcher.group(1) + */"5.html");
                        logger.info("lastPageUrl: {}", lastPageUrl);
                        push(new Request(lastPageUrl, "getPrePageAndSubject"));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("start", e);
        }
    }

    public void getPrePageAndSubject(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> links = doc.sel("//a");
            for (Object link : links) {
                if (link.toString().contains("上一页")) {
                    JXDocument tempDoc = new JXDocument(link.toString());
                    String prePageUrl = processHref(tempDoc.sel("//a/@href").get(0).toString());
                    logger.info("prePageUrl: {}", prePageUrl);
                    push(new Request(prePageUrl, "getPrePageAndSubject"));
                    break;
                }
            }
            List<Object> subjectLnks = doc.sel("//li[@class='p1 m1']/a");
            for (Object link : subjectLnks) {
                JXDocument tempDoc = new JXDocument(link.toString());
                String subjectUrl = processHref(tempDoc.sel("//a/@href").get(0).toString());
                String other = tempDoc.sel("//a/p[@class='other']/i/text()").get(0).toString();
                Map<String, String> params = new HashMap<>();
                params.put("other", other);
                push(new Request(subjectUrl, "getSubjectDetail").setMeta(params));
            }
        } catch (Exception e) {
            logger.error("getPrePageAndSubject", e);
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
            if (StringUtils.startsWith(poster, "//")) {
                poster = "http:" + poster;
            }
            
            String type2 = "";
            if (StringUtils.equals("电影", type) || StringUtils.equals("恐怖", type)) {
                type2 = "movie";
            } else if (StringUtils.equals("电视", type)) {
                type2 = "tv_show";
            } else if (StringUtils.equals("综艺", type)) {
                type2 = "variety";
            } else if (StringUtils.equals("动漫", type)) {
                type2 = "animation";
            } else if (StringUtils.equals("福利", type)) {
                type2 = "video";
            } else if (StringUtils.equals("音乐", type)) {
                type2 = "video";
            } else {
                return;
            }
            String finalType = type2;
            
            Subject subject = new Subject();
            subject.setType(finalType);
            subject.setTitle(title);
            subject.setGenres(StringUtils.equals("福利", type)
                    ? Collections.singleton(new Genre(finalType, "福利"))
                    : (StringUtils.equals("音乐", type)
                            ? Collections.singleton(new Genre(finalType, "音乐"))
                            : Arrays.asList(genres.split("/"))
                                .stream()
                                .map(s -> s.trim())
                                .filter(s -> !StringUtils.isEmpty(s))
                                .map(s -> StringUtils.equals("movie", finalType)
                                        || StringUtils.equals("tv_show", finalType) ? Utils.getFilteredGenreName(s) : s)
                                .distinct()
                                .map(s -> new Genre(finalType, s))
                                .filter(g -> !StringUtils.isEmpty(g.getName()))
                                .collect(Collectors.toSet())));
            subject.setProductionCountries(Arrays.asList(country.split("/"))
                    .stream()
                    .map(s -> s.trim())
                    .filter(s -> !StringUtils.isEmpty(s))
                    .map(s -> Utils.getFilteredProductionCountryName(s))
                    .distinct()
                    .map(s -> new ProductionCountry(finalType, s))
                    .filter(c -> !StringUtils.isEmpty(c.getName()))
                    .collect(Collectors.toSet()));
            subject.setVideos(videos
                    .stream()
                    .map(v -> {
                        JXDocument tempDoc = new JXDocument(v.toString());
                        Video video = new Video();
                        try {
                            video.setUrl(processHref(tempDoc.sel("//a/@href").get(0).toString()));
                            video.setSite("zxfuli");
                            video.setName(tempDoc.sel("//a/text()").get(0).toString());
                        } catch (Exception e) {
                            logger.error("getVideo", e);
                        }
                        return video;
                    })
                    .filter(v -> !StringUtils.isEmpty(v.getUrl()))
                    .collect(Collectors.toSet()));
            if (releaseDate.contains("/")) {
                releaseDate = releaseDate.split("/")[0];
            }
            if (!StringUtils.isEmpty(releaseDate)) {
                Pattern pattern = Pattern.compile("([0-9]{4}-[0-9]{2}-[0-9]{2})");
                Matcher matcher = pattern.matcher(releaseDate);
                if (matcher.find()) {
                    releaseDate = matcher.group(1);
                    subject.setReleaseDate(LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                } else {
                    pattern = Pattern.compile("([0-9]{4})");
                    matcher = pattern.matcher(releaseDate);
                    if (matcher.find()) {
                        releaseDate = matcher.group(1) + "-01-01";
                        subject.setReleaseDate(LocalDate.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    } else {
                        pattern = Pattern.compile("([0-9]{4}-[0-9]{1}-[0-9]{2})");
                        matcher = pattern.matcher(releaseDate);
                        if (matcher.find()) {
                            releaseDate = matcher.group(1);
                            subject.setReleaseDate(LocalDate.parse(releaseDate,
                                    DateTimeFormatter.ofPattern("yyyy-M-dd")));
                        } 
                    }
                }
            }
            subject.setOverview(overview);
            subject.setVoteAverage(Float.parseFloat(voteAverage));
            subject.setCrawlerId(response.getRealUrl());
            subject.setCreateTime(LocalDateTime.now());
            if (StringUtils.equals("video", finalType)) {
                subject.setThumbnails(Collections.singleton(new Thumbnail(subject, poster)));
            } else {
                subject.setPosters(Collections.singleton(new Poster(subject, poster)));
            }
            
            Set<Property> properties = new HashSet<>();
            properties.add(new Property(subject, "main_actor", StringUtils
                    .join(Arrays.asList(mainActor.split("/"))
                            .stream()
                            .map(s -> s.trim())
                            .collect(Collectors.toList()), ",")));
            properties.add(new Property(subject, "director", StringUtils
                    .join(Arrays.asList(director.split("/"))
                            .stream()
                            .map(s -> s.trim())
                            .collect(Collectors.toList()), ",")));
            properties.add(new Property(subject, "minutes", minutes.replace("分钟", "")));
            
            Map<String, String> meta = response.getMeta();
            if (subject.getReleaseDate() != null) {
                if (StringUtils.equals("movie", finalType)) {
                    String tmdbId = MdbIdGetter.getMovieTmdbId(subject.getTitle(), subject.getReleaseDate());
                    properties.add(new Property(subject, "tmdb_id", tmdbId));
                    MdbIdGetter.setTmdbInfo(subject, tmdbId);
                }
                if (meta == null || !StringUtils.equals("true", meta.get("ignore_third_info"))) {
                    if (StringUtils.equals("tv_show", finalType)) {
                        String tmdbId = MdbIdGetter.getTvShowTmdbId(subject.getTitle(), subject.getReleaseDate());
                        properties.add(new Property(subject, "tmdb_id", tmdbId));
                        MdbIdGetter.setTmdbInfo(subject, tmdbId);
                    }
                }
            }
            
            if (meta != null && !StringUtils.isEmpty(meta.get("other"))) {
                if (StringUtils.equals("tv_show", finalType)
                        || StringUtils.equals("variety", finalType)
                        || StringUtils.equals("animation", finalType)) {
                    String other = meta.get("other");
                    if (StringUtils.contains(other, "集已完结")) {
                        String s = StringUtils.trim(StringUtils.replace(other, "集已完结", ""));
                        properties.add(new Property(subject, "updated_episodes", s));
                        properties.add(new Property(subject, "total_episodes", s));
                        properties.add(new Property(subject, "completed", "true"));
                    } else {
                        Pattern p = Pattern.compile("更新至([0-9]+)集 / 共([0-9]+)集");
                        Matcher m = p.matcher(other);
                        if (m.find()) {
                            properties.add(new Property(subject, "updated_episodes", m.group(1)));
                            properties.add(new Property(subject, "total_episodes", m.group(2)));
                        }
                        properties.add(new Property(subject, "completed", "false"));
                    }
                }
            }
            subject.setProperties(properties.stream().filter(p -> !StringUtils.isEmpty(p.getValue())).collect(Collectors.toSet()));
            logger.debug(subject.toString());
            Subject newSubject = subjectService.create(subject);
            
            if (meta != null && !StringUtils.isEmpty(meta.get("recommend_title"))) {
                Recommend recommend = new Recommend(meta.get("recommend_title"),
                        finalType, meta.get("recommend_extra"), meta.get("recommend_extra1"), newSubject.getId(), LocalDateTime.now());
                recommendService.create(recommend);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getSubjectDetail", e);
        }
    }
    
    private static String processHref(String href) {
        if (StringUtils.isEmpty(href) || href.toLowerCase().contains("javascript")) {
            href = null;
            return href;
        } else {
            if (!StringUtils.startsWith(href, "http")) {
                href = "http://www.xinju5.com" + href;
            }
            href = StringUtils.replace(href, "http://www.wz80.com", "http://www.xinju5.com");
            return href;
        }
    }
}

package com.lcjian.spunsugar.crawler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.lcjian.spunsugar.util.StringUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.uwetrottmann.tmdb.Tmdb;
import com.uwetrottmann.tmdb.entities.FindResults;
import com.uwetrottmann.tmdb.enumerations.ExternalSource;

import us.codecraft.webmagic.selector.Html;

public class MdbIdGetter {

    private static OkHttpClient mClient = new OkHttpClient();

    private static Tmdb mTmdb = new Tmdb();
    
    {
        mClient.setConnectTimeout(1, TimeUnit.MINUTES);
        mClient.setReadTimeout(1, TimeUnit.MINUTES);
        mClient.setWriteTimeout(1, TimeUnit.MINUTES);
    }

    public static String getTmdbId(String imdbId) {
        mTmdb.setApiKey("f4fe5dbf051114ba829c94e8b0c47b6a");
        FindResults findResults = mTmdb.findService().find(imdbId, ExternalSource.IMDB_ID, "");
        if (findResults.movie_results != null && !findResults.movie_results.isEmpty()) {
            return String.valueOf(findResults.movie_results.get(0).id);
        }
        return null;
    }

    public static String getImdbId(String doubanId) {
        Request request = new Request.Builder().url("https://movie.douban.com/subject/" + doubanId + "/").build();
        Response response;
        try {
            response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {
                List<String> links = new Html(response.body().string()).xpath("//div[@id='info']/a").all();
                for (String link : links) {
                    if (link.contains("imdb")) {
                        return new Html(link).xpath("//a/text()").get();
                    }
                }
                return null;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDoubanId(String year, String title) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        String[] titles;
        if (title.contains("/")) {
            titles = title.split("/");
            for (int i = 0; i < titles.length; i++) {
                String doubanId = getDoubanId(year, titles[i]);
                if (!StringUtils.isEmpty(doubanId)) {
                    return doubanId;
                }
            }
            return null;
        } else {
            Request request = new Request.Builder().url("https://api.douban.com/v2/movie/search?q=" + title).build();
            Response response;
            try {
                response = mClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    SubjectResult result = (new Gson()).fromJson(response.body().string(), SubjectResult.class);
                    if (result.getTotal() == 0) {
                        return null;
                    } else {
                        List<Subject> subjects = result.getSubjects();
                        for (Subject subject : subjects) {
                            if (Integer.parseInt(year) == Integer.parseInt(subject.getYear())) {
                                return subject.getId();
                            }
                        }
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class SubjectResult {

        private int total;

        private List<Subject> subjects;

        public void setTotal(int total) {
            this.total = total;
        }

        public int getTotal() {
            return this.total;
        }

        public void setSubjects(List<Subject> subjects) {
            this.subjects = subjects;
        }

        public List<Subject> getSubjects() {
            return this.subjects;
        }

    }

    public class Subject {

        private String title;

        private String year;

        private String id;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}

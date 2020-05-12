package com.lcjian.spunsugar.util;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.lcjian.spunsugar.entity.Movie;
import com.lcjian.spunsugar.entity.TvShow;
import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.FindResults;
import com.uwetrottmann.tmdb2.enumerations.ExternalSource;

import cn.wanghaomiao.xpath.exception.XpathSyntaxErrorException;
import cn.wanghaomiao.xpath.model.JXDocument;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MdbIdGetter {

    private static OkHttpClient mClient = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build();
    
    private static Tmdb mTmdb = new Tmdb("f4fe5dbf051114ba829c94e8b0c47b6a");
    
    public static void setTmdbInfo(Movie movie) {
        if (!org.apache.commons.lang3.StringUtils.isEmpty(movie.getTmdbId())) {
            com.uwetrottmann.tmdb2.entities.Movie tmdbMovie;
            try {
                tmdbMovie = mTmdb.moviesService().summary(Integer.parseInt(movie.getTmdbId()), null, null).execute().body();
//                movie.setMovieGenres(new HashSet<>());
//                for (com.uwetrottmann.tmdb2.entities.Genre genre : tmdbMovie.genres) {
//                    movie.getMovieGenres().add(new MovieGenre(genre.name));
//                }
//                if (tmdbMovie.release_date != null) {
//                    movie.setReleaseDate(tmdbMovie.release_date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//                }
                movie.setVoteAverage(tmdbMovie.vote_average.floatValue());
                movie.setPopularity(tmdbMovie.popularity.floatValue());

//                movie.setMovieProductionCountries(new HashSet<>());
//                for (com.uwetrottmann.tmdb2.entities.ProductionCountry productionCountry : tmdbMovie.production_countries) {
//                    MovieProductionCountry movieProductionCountry = new MovieProductionCountry();
//                    movieProductionCountry.setIso_3166_1(productionCountry.iso_3166_1);
//                    movieProductionCountry.setName(productionCountry.name);
//                    movie.getMovieProductionCountries().add(movieProductionCountry);
//                }
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void setTmdbInfo(TvShow tvShow) {
        if (!org.apache.commons.lang3.StringUtils.isEmpty(tvShow.getTmdbId())) {
            com.uwetrottmann.tmdb2.entities.TvSeason tmdbTvSeason;
            try {
                tmdbTvSeason = mTmdb.tvSeasonsService().season(Integer.parseInt(tvShow.getTmdbId()), 1, null, null).execute().body();
                tvShow.setVoteAverage(tmdbTvSeason.episodes.get(0).vote_average.floatValue());
                tvShow.setPopularity(tmdbTvSeason.episodes.get(0).vote_count);
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getTmdbId(String imdbId, String subtype) {
        FindResults findResults;
        try {
            findResults = mTmdb.findService().find(imdbId, ExternalSource.IMDB_ID, "").execute().body();
            if (StringUtils.equals(subtype, "movie")) {
                if (findResults.movie_results != null
                        && !findResults.movie_results.isEmpty()) {
                    return String.valueOf(findResults.movie_results.get(0).id);
                }
            } else {
                if (findResults.tv_season_results != null
                        && !findResults.tv_season_results.isEmpty()) {
                    return String.valueOf(findResults.tv_season_results.get(0).id);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getImdbId(String doubanId) {
        Request request = new Request.Builder().url(
                "https://movie.douban.com/subject/" + doubanId + "/").build();
        Response response;
        try {
            response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {
                List<Object> links = new JXDocument(response.body().string()).sel("//div[@id='info']/a");
                for (Object link : links) {
                    if (link.toString().contains("imdb")) {
                        return new JXDocument(link.toString()).sel("//a/text()").get(0).toString();
                    }
                }
                return null;
            } else {
                response.close();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDoubanId(int year, String title, String subtype) {
        String[] titles;
        if (title.contains("/") || title.contains(" ")) {
            if (title.contains("/")) {
                titles = title.split("/");
                for (int i = 0; i < titles.length; i++) {
                    String doubanId = getDoubanId(year, titles[i], subtype);
                    if (!org.apache.commons.lang3.StringUtils.isEmpty(doubanId)) {
                        return doubanId;
                    }
                }
            } else {
                titles = title.split(" ");
                for (int i = 0; i < titles.length; i++) {
                    String doubanId = getDoubanId(year, titles[i], subtype);
                    if (!org.apache.commons.lang3.StringUtils.isEmpty(doubanId)) {
                        return doubanId;
                    }
                }
            }
            return null;
        } else {
            synchronized (MdbIdGetter.class) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                Request request = new Request.Builder().url("https://api.douban.com/v2/movie/search?q=" + title).build();
                Response response;
                try {
                    response = mClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        SubjectResult result = (new Gson()).fromJson(response
                                .body().string(), SubjectResult.class);
                        if (result.getTotal() == 0) {
                            return null;
                        } else {
                            List<Subject> subjects = result.getSubjects();
                            for (Subject subject : subjects) {
                                if (subject.getYear().contains("年")) {
                                    subject.setYear(subject.getYear().replace("年", ""));
                                }
                                if (subject.getYear().contains("—至今")) {
                                    subject.setYear(subject.getYear().replace("—至今", ""));
                                }
                                if (!org.apache.commons.lang3.StringUtils.isEmpty(subject.getYear())) {
                                    if (year == Integer.parseInt(subject.getYear()) && StringUtils.equals(subtype, subject.getSubtype())) {
                                        return subject.getId();
                                    }
                                }
                            }
                            return null;
                        }
                    } else {
                        response.close();
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
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
        
        private String subtype;

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

        public String getSubtype() {
            return subtype;
        }

        public void setSubtype(String subtype) {
            this.subtype = subtype;
        }
    }

}

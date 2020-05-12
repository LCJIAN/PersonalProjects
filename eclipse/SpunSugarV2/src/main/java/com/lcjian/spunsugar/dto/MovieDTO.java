package com.lcjian.spunsugar.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.lcjian.spunsugar.entity.Movie;

public class MovieDTO {

    private Integer id;

    private String title;

    private String overview;

    private String poster;
    
    private String director;
    
    private String mainActor;
    
    private Integer minutes;

    private Float voteAverage;

    private Float popularity;

    private LocalDate releaseDate;

    private String imdbId;

    private String doubanId;

    private String tmdbId;

    private LocalDateTime createTime;

    private String crawlerId;

    public MovieDTO() {
    }
    
    public MovieDTO(Movie movie) {
        super();
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.overview = movie.getOverview();
        this.poster = movie.getPoster();
        this.director = movie.getDirector();
        this.mainActor = movie.getMainActor();
        this.minutes = movie.getMinutes();
        this.voteAverage = movie.getVoteAverage();
        this.popularity = movie.getPopularity();
        this.releaseDate = movie.getReleaseDate();
        this.imdbId = movie.getImdbId();
        this.doubanId = movie.getDoubanId();
        this.tmdbId = movie.getTmdbId();
        this.createTime = movie.getCreateTime();
        this.crawlerId = movie.getCrawlerId();
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return this.overview;
    }
    
    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getMainActor() {
        return mainActor;
    }

    public void setMainActor(String mainActor) {
        this.mainActor = mainActor;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getPoster() {
        return this.poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public Float getVoteAverage() {
        return this.voteAverage;
    }

    public void setVoteAverage(Float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public LocalDate getReleaseDate() {
        return this.releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getImdbId() {
        return this.imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getDoubanId() {
        return this.doubanId;
    }

    public void setDoubanId(String doubanId) {
        this.doubanId = doubanId;
    }

    public String getCrawlerId() {
        return crawlerId;
    }

    public void setCrawlerId(String crawlerId) {
        this.crawlerId = crawlerId;
    }

    public String getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(String tmdbId) {
        this.tmdbId = tmdbId;
    }

    public Float getPopularity() {
        return popularity;
    }

    public void setPopularity(Float popularity) {
        this.popularity = popularity;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

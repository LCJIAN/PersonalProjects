package com.lcjian.spunsugar.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "tv_show")
public class TvShow implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "overview", columnDefinition = "MEDIUMTEXT")
    private String overview;

    @Column(name = "poster", length = 100)
    private String poster;
    
    @Column(name = "director", length = 100)
    private String director;
    
    @Column(name = "main_actor", length = 200)
    private String mainActor;
    
    @Column(name = "minutes")
    private Integer minutes;

    @Column(name = "vote_average")
    private Float voteAverage;

    @Column(name = "popularity")
    private Integer popularity;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "imdb_id", length = 45)
    private String imdbId;

    @Column(name = "douban_id", length = 45)
    private String doubanId;

    @Column(name = "tmdb_id", length = 45)
    private String tmdbId;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "crawler_id", unique = true, nullable = false, length = 100)
    private String crawlerId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tvShow")
    private Set<TvShowVideo> tvShowVideos = new HashSet<TvShowVideo>(0);

    @ManyToMany
    @JoinTable(name = "tv_show_genre_reference",
               joinColumns = { @JoinColumn(name = "tv_show_id", referencedColumnName = "id") },
               inverseJoinColumns = { @JoinColumn(name = "tv_show_genre_id", referencedColumnName = "id") })
    private Set<TvShowGenre> tvShowGenres = new HashSet<TvShowGenre>(0);
    
    @ManyToMany
    @JoinTable(name = "tv_show_production_country_reference",
               joinColumns = { @JoinColumn(name = "tv_show_id", referencedColumnName = "id") },
               inverseJoinColumns = { @JoinColumn(name = "production_country_id", referencedColumnName = "id") })
    private Set<TvShowProductionCountry> tvShowProductionCountries = new HashSet<TvShowProductionCountry>(0);

    public TvShow() {
    }

    public TvShow(String title) {
        this.title = title;
    }

    public TvShow(String title, String overview, String poster,
            Float voteAverage, LocalDate releaseDate, String imdbId,
            String doubanId, Set<TvShowVideo> tvShowVideos) {
        this.title = title;
        this.overview = overview;
        this.poster = poster;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.imdbId = imdbId;
        this.doubanId = doubanId;
        this.tvShowVideos = tvShowVideos;
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

    public String getPoster() {
        return this.poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
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

    public Set<TvShowVideo> getTvShowVideos() {
        return this.tvShowVideos;
    }

    public void setTvShowVideos(Set<TvShowVideo> tvShowVideos) {
        this.tvShowVideos = tvShowVideos;
    }

    public Set<TvShowGenre> getTvShowGenres() {
        return tvShowGenres;
    }

    public void setTvShowGenres(Set<TvShowGenre> tvShowGenres) {
        this.tvShowGenres = tvShowGenres;
    }

    public String getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(String tmdbId) {
        this.tmdbId = tmdbId;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Set<TvShowProductionCountry> getTvShowProductionCountries() {
        return tvShowProductionCountries;
    }

    public void setTvShowProductionCountries(
            Set<TvShowProductionCountry> tvShowProductionCountries) {
        this.tvShowProductionCountries = tvShowProductionCountries;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

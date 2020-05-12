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

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "movie")
public class Movie implements java.io.Serializable {

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
    private Float popularity;

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

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "movie")
    private Set<MovieVideo> movieVideos = new HashSet<MovieVideo>(0);

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "movie_genre_reference",
               joinColumns = { @JoinColumn(name = "movie_id", referencedColumnName = "id") },
               inverseJoinColumns = { @JoinColumn(name = "movie_genre_id", referencedColumnName = "id") })
    private Set<MovieGenre> movieGenres = new HashSet<MovieGenre>(0);
    
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "movie_production_country_reference",
               joinColumns = { @JoinColumn(name = "movie_id", referencedColumnName = "id") },
               inverseJoinColumns = { @JoinColumn(name = "production_country_id", referencedColumnName = "id") })
    private Set<MovieProductionCountry> movieProductionCountries = new HashSet<MovieProductionCountry>(0);

    public Movie() {
    }

    public Movie(String title) {
        this.title = title;
    }

    public Movie(String title, String overview, String poster,
            Float voteAverage, LocalDate releaseDate, String imdbId,
            String doubanId, Set<MovieVideo> movieVideos) {
        this.title = title;
        this.overview = overview;
        this.poster = poster;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.imdbId = imdbId;
        this.doubanId = doubanId;
        this.movieVideos = movieVideos;
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

    public Set<MovieVideo> getMovieVideos() {
        return this.movieVideos;
    }

    public void setMovieVideos(Set<MovieVideo> movieVideos) {
        this.movieVideos = movieVideos;
    }

    public Set<MovieGenre> getMovieGenres() {
        return movieGenres;
    }

    public void setMovieGenres(Set<MovieGenre> movieGenres) {
        this.movieGenres = movieGenres;
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

    public Set<MovieProductionCountry> getMovieProductionCountries() {
        return movieProductionCountries;
    }

    public void setMovieProductionCountries(
            Set<MovieProductionCountry> movieProductionCountries) {
        this.movieProductionCountries = movieProductionCountries;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

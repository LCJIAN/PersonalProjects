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
@Table(name = "subject")
public class Subject implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "type", nullable = false, length = 45)
    private String type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "overview", columnDefinition = "MEDIUMTEXT")
    private String overview;

    @Column(name = "vote_average")
    private Float voteAverage;

    @Column(name = "popularity")
    private Float popularity;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "crawler_id", unique = true, nullable = false, length = 100)
    private String crawlerId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "subject")
    private Set<Poster> posters = new HashSet<Poster>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "subject")
    private Set<Thumbnail> thumbnails = new HashSet<Thumbnail>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "subject")
    private Set<Video> videos = new HashSet<Video>(0);

    @ManyToMany
    @JoinTable(name = "subject_genre_reference",
               joinColumns = { @JoinColumn(name = "subject_id", referencedColumnName = "id") },
               inverseJoinColumns = {
                       @JoinColumn(name = "subject_genre_id", referencedColumnName = "id") })
    private Set<Genre> genres = new HashSet<Genre>(0);

    @ManyToMany
    @JoinTable(name = "subject_production_country_reference",
              joinColumns = { @JoinColumn(name = "subject_id", referencedColumnName = "id") },
              inverseJoinColumns = { @JoinColumn(name = "subject_production_country_id", referencedColumnName = "id") })
    private Set<ProductionCountry> productionCountries = new HashSet<ProductionCountry>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "subject")
    private Set<Property> properties = new HashSet<Property>(0);

    public Subject() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Float getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public Float getPopularity() {
        return popularity;
    }

    public void setPopularity(Float popularity) {
        this.popularity = popularity;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getCrawlerId() {
        return crawlerId;
    }

    public void setCrawlerId(String crawlerId) {
        this.crawlerId = crawlerId;
    }

    public Set<Poster> getPosters() {
        return posters;
    }

    public void setPosters(Set<Poster> posters) {
        this.posters = posters;
    }

    public Set<Thumbnail> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Set<Thumbnail> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public Set<Video> getVideos() {
        return videos;
    }

    public void setVideos(Set<Video> videos) {
        this.videos = videos;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public Set<ProductionCountry> getProductionCountries() {
        return productionCountries;
    }

    public void setProductionCountries(Set<ProductionCountry> productionCountries) {
        this.productionCountries = productionCountries;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public void setProperties(Set<Property> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

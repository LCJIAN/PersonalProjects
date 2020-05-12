package com.lcjian.spunsugar.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "video")
public class Video implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "movie_id", referencedColumnName = "id", nullable = false)
    private Movie movie;

    @Column(name = "url", length = 300, nullable = false)
    private String url;

    @Column(name = "site", length = 45)
    private String site;

    @Column(name = "name", length = 100)
    private String name;

    public Video() {
    }

    public Video(Movie movie, String url, String site) {
        this.movie = movie;
        this.url = url;
        this.site = site;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Movie getMovie() {
        return this.movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSite() {
        return this.site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

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
@Table(name = "tv_show_video")
public class TvShowVideo implements java.io.Serializable {

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
    @JoinColumn(name = "tv_show_id", referencedColumnName = "id", nullable = false)
    private TvShow tvShow;

    @Column(name = "url", length = 300, nullable = false)
    private String url;

    @Column(name = "site", length = 45)
    private String site;

    @Column(name = "name", length = 100)
    private String name;

    public TvShowVideo() {
    }

    public TvShowVideo(TvShow tvShow, String url, String site) {
        this.tvShow = tvShow;
        this.url = url;
        this.site = site;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TvShow getTvShow() {
        return this.tvShow;
    }

    public void setTvShow(TvShow tvShow) {
        this.tvShow = tvShow;
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

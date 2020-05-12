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
@Table(name = "tv_live_source")
public class TvLiveSource implements java.io.Serializable {

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
    @JoinColumn(name = "tv_station_id", referencedColumnName = "id", nullable = false)
    private TvStation tvStation;

    @Column(name = "url", length = 200)
    private String url;

    @Column(name = "site", length = 45)
    private String site;

    public TvLiveSource() {
    }

    public TvLiveSource(TvStation tvStation) {
        this.tvStation = tvStation;
    }

    public TvLiveSource(TvStation tvStation, String url, String site) {
        this.tvStation = tvStation;
        this.url = url;
        this.site = site;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TvStation getTvStation() {
        return this.tvStation;
    }

    public void setTvStation(TvStation tvStation) {
        this.tvStation = tvStation;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

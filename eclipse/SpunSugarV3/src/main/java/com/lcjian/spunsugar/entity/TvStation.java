package com.lcjian.spunsugar.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tv_station")
public class TvStation implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "logo", length = 100)
    private String logo;

    @Column(name = "type", length = 45)
    private String type;

    @Column(name = "channel", nullable = false, unique = true, length = 45)
    private String channel;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "tvStation")
    private Set<TvLiveSource> tvLiveSources = new HashSet<TvLiveSource>(0);

    public TvStation() {
    }

    public TvStation(String name) {
        this.name = name;
    }

    public TvStation(String name, String logo, String type,
            Set<TvLiveSource> tvLiveSources) {
        this.name = name;
        this.logo = logo;
        this.type = type;
        this.tvLiveSources = tvLiveSources;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<TvLiveSource> getTvLiveSources() {
        return this.tvLiveSources;
    }

    public void setTvLiveSources(Set<TvLiveSource> tvLiveSources) {
        this.tvLiveSources = tvLiveSources;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

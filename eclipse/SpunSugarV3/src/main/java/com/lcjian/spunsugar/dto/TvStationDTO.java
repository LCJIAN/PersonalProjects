package com.lcjian.spunsugar.dto;

import java.io.Serializable;
import java.util.List;

import com.lcjian.spunsugar.entity.TvLiveSource;

public class TvStationDTO implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String name;

    private String logo;

    private String type;

    private String channel;

    private List<TvLiveSource> tvLiveSources;

    private ProgramDTO now;

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

    public List<TvLiveSource> getTvLiveSources() {
        return this.tvLiveSources;
    }

    public void setTvLiveSources(List<TvLiveSource> tvLiveSources) {
        this.tvLiveSources = tvLiveSources;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public ProgramDTO getNow() {
        return now;
    }

    public void setNow(ProgramDTO now) {
        this.now = now;
    }
}

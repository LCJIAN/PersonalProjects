package com.lcjian.spunsugar.dto;

import com.lcjian.spunsugar.entity.Video;

public class VideoDTO {

    private Integer id;

    private String url;

    private String site;

    private String name;

    public VideoDTO() {
    }
    
    public VideoDTO(Video video) {
        this.id = video.getId();
        this.url = video.getUrl();
        this.site = video.getSite();
        this.name = video.getName();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSite() {
        return site;
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
}

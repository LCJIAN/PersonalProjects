package com.lcjian.spunsugar.entity;

import com.google.gson.annotations.Expose;

public class Config implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    @Expose
    private Integer id;
    @Expose
    private Boolean hasLive;
    @Expose
    private Boolean hasMovie;
    @Expose
    private Boolean hasShare;
    @Expose
    private String shareContent;
    @Expose
    private String shareUrl;

    public Config() {
    }

    public Config(Boolean hasLive) {
        this.hasLive = hasLive;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean isHasLive() {
        return hasLive;
    }

    public void setHasLive(Boolean hasLive) {
        this.hasLive = hasLive;
    }

    public Boolean isHasShare() {
        return hasShare;
    }

    public void setHasShare(Boolean hasShare) {
        this.hasShare = hasShare;
    }

    public String getShareContent() {
        return shareContent;
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public Boolean isHasMovie() {
        return hasMovie;
    }

    public void setHasMovie(Boolean hasMovie) {
        this.hasMovie = hasMovie;
    }
}

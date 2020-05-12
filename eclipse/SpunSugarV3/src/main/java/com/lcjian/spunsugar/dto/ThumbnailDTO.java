package com.lcjian.spunsugar.dto;

import com.lcjian.spunsugar.entity.Thumbnail;

public class ThumbnailDTO {

    private String url;
    
    public ThumbnailDTO(Thumbnail t) {
        this.setUrl(t.getUrl());
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

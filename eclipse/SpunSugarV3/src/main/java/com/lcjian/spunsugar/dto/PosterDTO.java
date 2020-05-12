package com.lcjian.spunsugar.dto;

import com.lcjian.spunsugar.entity.Poster;

public class PosterDTO {

    private String url;
    
    public PosterDTO(Poster p) {
        this.setUrl(p.getUrl());
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}

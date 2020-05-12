package com.lcjian.vastplayer.data.network.entity;

public class VideoUrl implements java.io.Serializable {

    /**
     * serialVersionUID
     */
    public static final long serialVersionUID = 1L;

    public Long id;
    public String url;
    public String name;
    public String site;
    public String type;  // "movie", "tv_show", "tv_station", "direct"

    public Long parentId;
}
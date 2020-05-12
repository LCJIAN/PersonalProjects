package com.lcjian.vastplayer.data.network.entity;

import com.lcjian.vastplayer.data.db.entity.Favourite;
import com.lcjian.vastplayer.data.db.entity.WatchHistory;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Subject implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public Long id;
    public String type;
    public String title;
    public String overview;
    public Float vote_average;
    public Float popularity;
    public Date release_date;
    public List<Poster> posters;
    public List<Thumbnail> thumbnails;
    public List<Genre> genres;
    public List<Country> production_countries;
    public List<Backdrop> backdrops;
    public Map<String, String> properties;
    public Integer vote_count;

    public WatchHistory watchHistory;
    public Favourite favourite;
}

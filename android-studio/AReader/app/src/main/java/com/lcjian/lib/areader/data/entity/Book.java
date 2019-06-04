package com.lcjian.lib.areader.data.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Book implements Displayable, Serializable {

    @SerializedName("book_id")
    public Long id;
    @SerializedName("book_name")
    public String name;
    @SerializedName("author")
    public String author;
    @SerializedName("face_url")
    public String poster;
    @SerializedName("intro")
    public String introduction;
    @SerializedName("cate_name")
    public String categoryName;
    @SerializedName("status")
    public Integer status;

    // for detail
    @SerializedName("cate_id")
    public Long categoryId;
    @SerializedName("last_name")
    public String lastName;
    @SerializedName("last_index")
    public Integer lastIndex;
    @SerializedName("last_uptime")
    public Long lastUpTime;

    public String progress;

    public int showMode; // 0:list, 1:grid
}

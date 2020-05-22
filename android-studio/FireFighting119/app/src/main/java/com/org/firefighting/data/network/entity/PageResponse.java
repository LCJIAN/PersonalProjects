package com.org.firefighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PageResponse<T> {

    public String code;
    public String message;
    public Integer page;
    public Integer pager;
    @SerializedName(value = "total", alternate = "totalElements")
    public Integer total;
    @SerializedName(value = "result", alternate = {"content", "list"})
    public List<T> result;
}

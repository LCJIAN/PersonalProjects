package com.org.firefighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ResourceDataRequest {

    public Map<String, String> searchMap;

    @SerializedName("rowIdOnly")
    public Integer rowIdOnly = 0;
    @SerializedName("search")
    public String search;
    @SerializedName("page")
    public Integer pageNumber;
    @SerializedName("size")
    public Integer pageSize;

    public static class Option {
        public String op;
        public String filedName;
        public String keyword;
    }
}

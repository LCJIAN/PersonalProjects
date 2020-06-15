package com.org.firefighting.data.network.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class ResourceDataRequest {

    @SerializedName("rowIdOnly")
    public Integer rowIdOnly = 0;
    @SerializedName("search")
    public Map<String, String> search;
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

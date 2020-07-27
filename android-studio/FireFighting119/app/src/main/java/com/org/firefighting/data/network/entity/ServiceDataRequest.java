package com.org.firefighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ServiceDataRequest {

    @SerializedName("rowIdOnly")
    public Integer rowIdOnly = 0;
    @SerializedName("search")
    public List<String> search;
    @SerializedName("pageNum")
    public Integer pageNumber;
    @SerializedName("size")
    public Integer pageSize;

    public static class Option {
        public String op;
        public String filedName;
        public String keyword;
    }
}

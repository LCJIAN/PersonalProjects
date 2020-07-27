package com.org.firefighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class ServiceDataRequestEmpty {

    @SerializedName("rowIdOnly")
    public Integer rowIdOnly = 0;
    @SerializedName("search")
    public String search;
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

package com.lcjian.mmt.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponsePageData<T> {

    @SerializedName("total")
    public Integer total_elements;
    @SerializedName("rows")
    public List<T> elements;
}

package com.lcjian.lib.areader.data.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BooksReadInfo {

    @SerializedName("book")
    public List<BookReadInfo> books;
    @SerializedName("dev_id")
    public String devId;
}

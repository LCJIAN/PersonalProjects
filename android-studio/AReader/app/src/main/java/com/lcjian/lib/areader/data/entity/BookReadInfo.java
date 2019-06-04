package com.lcjian.lib.areader.data.entity;

import com.google.gson.annotations.SerializedName;

public class BookReadInfo {

    @SerializedName("book_id")
    public Long bookId;
    @SerializedName("read_time")
    public Long readTime;
}

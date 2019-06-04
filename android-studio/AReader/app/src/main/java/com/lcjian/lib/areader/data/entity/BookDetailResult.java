package com.lcjian.lib.areader.data.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BookDetailResult {

    @SerializedName("bookInfo")
    public Book detail;

    @SerializedName("allLike")
    public List<Book> allLikeBooks;
    @SerializedName("similar")
    public List<Book> similarBooks;
}

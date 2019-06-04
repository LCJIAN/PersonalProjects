package com.lcjian.lib.areader.data.entity;

import com.google.gson.annotations.SerializedName;

public class SlideBook {

    @SerializedName("book_id")
    public Long id;
    @SerializedName("slide_img")
    public String image;
    @SerializedName("redirect")
    public String redirect;
}

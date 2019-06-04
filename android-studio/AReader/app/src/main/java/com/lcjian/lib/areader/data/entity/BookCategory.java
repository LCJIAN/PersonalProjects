package com.lcjian.lib.areader.data.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class BookCategory implements Serializable, Displayable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    public Long id;
    @SerializedName("name")
    public String name;
    @SerializedName("cover")
    public List<String> covers;
}

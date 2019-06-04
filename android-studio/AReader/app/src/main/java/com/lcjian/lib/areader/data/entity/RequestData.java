package com.lcjian.lib.areader.data.entity;

import com.google.gson.annotations.SerializedName;

public class RequestData<T> {

    @SerializedName("data")
    public T data;

}

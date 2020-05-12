package com.org.firefighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class ResponseData<T> {

    public Integer code;
    public String message;
    @SerializedName("result")
    public T data;
}

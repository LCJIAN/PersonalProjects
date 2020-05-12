package com.winside.lighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class ResponseData<T> {

    @SerializedName("code")
    public Integer code;
    @SerializedName("msg")
    public String message;
    @SerializedName("result")
    public T data;

}

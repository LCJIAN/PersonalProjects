package com.winside.lighting.data.network;

import com.google.gson.annotations.SerializedName;

public class ResponseData<T> {

    @SerializedName("code")
    public Integer code;
    @SerializedName("message")
    public String message;
    @SerializedName("result")
    public T data;

}

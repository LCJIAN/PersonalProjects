package com.lcjian.cloudlocation.data.entity;

import com.google.gson.annotations.SerializedName;

public class ResponseData<T> {

    @SerializedName("rows")
    public T data;
    @SerializedName("code")
    public Integer code;
}

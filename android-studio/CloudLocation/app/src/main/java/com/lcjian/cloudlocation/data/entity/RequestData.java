package com.lcjian.cloudlocation.data.entity;

import com.google.gson.annotations.SerializedName;

public class RequestData<T> {

    @SerializedName("data")
    public T data;

}

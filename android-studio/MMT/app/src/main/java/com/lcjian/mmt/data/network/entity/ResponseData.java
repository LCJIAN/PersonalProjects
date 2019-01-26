package com.lcjian.mmt.data.network.entity;

public class ResponseData<T> {

    public Integer code;
    public String message;
    public String token;
    public T data;

}

package com.lcjian.osc.data.network.entity;

public class ResponseData<T> {

    public T result;

    public String targetUrl;
    public Boolean success;
    public ErrorMsg error;
    public Boolean unAuthorizedRequest;
    public Boolean __abp;

    public static class ErrorMsg {
        public String code;
        public String message;
        public String details;
        public String validationErrors;
    }
}

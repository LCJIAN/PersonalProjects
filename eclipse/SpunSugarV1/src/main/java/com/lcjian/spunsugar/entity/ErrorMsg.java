package com.lcjian.spunsugar.entity;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class ErrorMsg implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    @Expose
    public int code;
    @Expose
    public String message;
    
    public ErrorMsg() {
        super();
    }

    public ErrorMsg(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }
}

package com.lcjian.spunsugar.entity;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class Program implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    @Expose
    private String name;
    @Expose
    private String time;
    @Expose
    private boolean isLive;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean isLive) {
        this.isLive = isLive;
    }
}

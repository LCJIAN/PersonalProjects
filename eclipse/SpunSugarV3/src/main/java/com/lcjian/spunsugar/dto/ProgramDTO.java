package com.lcjian.spunsugar.dto;

import java.io.Serializable;

public class ProgramDTO implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    
    private String name;
    
    private String time;
    
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

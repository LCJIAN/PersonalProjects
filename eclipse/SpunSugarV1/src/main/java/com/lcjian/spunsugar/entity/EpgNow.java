package com.lcjian.spunsugar.entity;

import java.io.Serializable;

public class EpgNow implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private String t;
    private String s;
    private String c;
    private String n;
    private String e;

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }
}

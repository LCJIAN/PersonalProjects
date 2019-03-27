package com.lcjian.mmt.data.network.entity;

import java.io.Serializable;

public class Message implements Serializable {

    public String id;
    public Integer msgStatus; // 1：未读。2：已读
    public String content;
    public Long createDate;
}

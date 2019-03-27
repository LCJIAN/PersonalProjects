package com.lcjian.mmt.data.network.entity;

import java.io.Serializable;

public class Inquiry implements Serializable {

    public String id;
    public Long inquiryTime;
    public Store store; // 卸货仓库
    public String unloadAddr; // 卸货地址
    public Long invalidTime; // 失效时间
}

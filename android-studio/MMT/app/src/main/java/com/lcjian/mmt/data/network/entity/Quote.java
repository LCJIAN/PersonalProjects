package com.lcjian.mmt.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class Quote {

    public String id;
    @SerializedName("products")
    public Product product;
    public Inquiry inquiry;
    @SerializedName("offerTpriceTime")
    public Long quoteTime; // 报价时间
    public String taxRate; // 税率(%)
    public String status; //状态1.报价中;2.已报价;3.报价失效;4.已结束;9.运输中
    public String distance; // 距离(Km)
    public Long invalidTime; // 失效时间


}

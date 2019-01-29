package com.lcjian.mmt.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class TranOrder {

    public String id; // 运输订单ID
    public String tranOrderCode; // 运单号
    public String tranStatus;
    @SerializedName("products")
    public Product product;
    public Integer quantity; // 数量(基础单位*1000)
    public Integer quantityDiff;
    public Double amount; // 总运费（分）

}

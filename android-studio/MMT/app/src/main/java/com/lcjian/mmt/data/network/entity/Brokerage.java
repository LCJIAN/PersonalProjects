package com.lcjian.mmt.data.network.entity;

public class Brokerage {

    public String id;
    public Double amount; // 佣金金额单位：分
    public Integer broType; // 佣金类型(1.销售2.采购3.物流4.货主)
    public Integer invoiceStatus; // 发票状态。0：未开票。1：已开票。2：开票中
    public Long createDate;
    public TransOrder mmtOrder;
    public Deposit mmtDeposit;
    public CarOrder mmtTransOrder;
    public Product.Merchant mmtMerchants;
    public Product mmtProducts;
}

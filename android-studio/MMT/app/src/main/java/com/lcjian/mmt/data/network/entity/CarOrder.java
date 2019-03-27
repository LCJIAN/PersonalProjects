package com.lcjian.mmt.data.network.entity;

import java.io.Serializable;

public class CarOrder implements Serializable {

    public String id; // 分车订单ID分车订单ID
    public String tranOrderCode; // 分车订单号
    public Double price; // 运费单价（分/吨）
    public Integer quantity; // 数量(基数*1000)
    public Double amount; // 总运费（分）
    public String commTax; // 物流商佣金率（相对值单位%,绝对值单位分）
    public Double commAmount; // 佣金（分）
    public Long arrivalTime; // 预计最早到达装货工厂时间
    public Store loadStore; // 装货仓库
    public Long unloadTime; // 预计到达卸货工厂时间
    public Store unloadStore; // 卸货仓库
    public String status; // 状态0.空车未出发；1.已出车；2.待装货；3运输中；4待卸货；5结束；6.其他
    public Long loadTime; // 开始装货的时间
    public Long unloadeTime; // 开始卸货的时间
    public Integer invoice; // 发票。 0：未开发票；1：已开发票
    public Car cars;
    public Product mmtProducts;
    public Integer poundStatus; // 过磅单状态 0.不需要确认，大于0 需要确认
    public Object poundBeforeload;
    public Object poundAfterload;
    public Object poundBeforeUnload;
    public Object poundAfterUnload;

    public Object weighBeforeload;
    public Object weighAfterload;
    public Object weighBeforeunload;
    public Object weighAfterunload;

    public TransOrder mmtOrder;
}

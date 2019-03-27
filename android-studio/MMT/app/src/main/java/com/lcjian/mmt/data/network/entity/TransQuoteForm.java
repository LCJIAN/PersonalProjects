package com.lcjian.mmt.data.network.entity;

import java.io.Serializable;

public class TransQuoteForm implements Serializable {

    public String trucksId;
    public Long arrivalTime; // 预计最早到达装货工厂时间
    public Long unloadTime; // 预计到达卸货工厂时间
    public Integer abletranNum; // 可装运趟数(字典控制了最大3次)
    public Double tprice; // 带票运输价格单价（分/吨）
    public Double utprice; // 不带票运输价格单价（分/吨）
    public String timeConsuming; // 大约耗时
    public Integer carryingCapacity; // 载重(吨)

    public Car car;
}

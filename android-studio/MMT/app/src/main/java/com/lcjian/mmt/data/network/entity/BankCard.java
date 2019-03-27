package com.lcjian.mmt.data.network.entity;

import java.io.Serializable;

public class BankCard implements Serializable {

    public String id;
    public String remarks; // 银行卡logo颜色
    public String cardNo; // 银行卡号
    public String openBank; // 开户行名称
    public String ownerName; // 持卡人姓名
    public String cardType; // 卡类型
    public String logoUrl; // 卡logo
}

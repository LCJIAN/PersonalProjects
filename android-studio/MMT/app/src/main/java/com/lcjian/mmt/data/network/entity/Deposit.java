package com.lcjian.mmt.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class Deposit {

    @SerializedName("balance1")
    public Double balance; // 账户余额(单位分)
    @SerializedName("amount")
    public Double bondBase; // 保证金基准值(单位分)
    @SerializedName("balance")
    public Double bondBalance; // 保证金账户余额(单位分)

}

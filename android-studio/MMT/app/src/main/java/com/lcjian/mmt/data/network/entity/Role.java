package com.lcjian.mmt.data.network.entity;

public class Role {

    public String id;
    public String authType; // 认证类型(1:销售 2:采购 3:运输 4:货主)
    public String authStatus; // 认证状态(0:审核通过 1:认证中 2:驳回 4:未认证)

}

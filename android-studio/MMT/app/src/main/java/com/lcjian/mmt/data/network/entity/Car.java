package com.lcjian.mmt.data.network.entity;

import java.util.List;

public class Car {

    public String id;
    public String carCode; // 车牌号
    public String carSize; // 车辆整体大小。格式： L*W*H
    public String boxSize; // 货厢大小。格式： L*W*H
    public String boxType; // 货厢样式
    public String goodsType; // 可装载的货物类型：两位:第一位代表形态:1固体，2液体，3气体;第二位:1危化品(特殊商品，上架需要审核)，2普通货物，3生鲜类货物
    public String carType; // 车辆类型
    public Double loadWeight; // 核定载重单位（基础单位*1000)
    public String merchId;
    public String ownerId; // 车主信息id
    public Driver driver1; // 主司机
    public String carStatus; // 车辆状态:0.空车；1.锁定；2.去装货途中；3.装货中；4.载货运输中；5.卸货中；6.其他
    public String checkStatus; // 车辆认证状态0.审核中(未认证)1.已认证2.未通过
    public List<ProductType> productTypes; // 危化品时，需要指明商品类型 K(id)-V商品类型ID，K(name)-V商品类型名称
    public Owner owner;

    public static class Owner {
        public String id;
        public String name;
        public String phone;
        public String mobile;
    }
}

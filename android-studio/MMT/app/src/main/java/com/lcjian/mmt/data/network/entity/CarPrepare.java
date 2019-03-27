package com.lcjian.mmt.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class CarPrepare implements Serializable {

    public String carCode; // 车牌号
    public String carSize; // 车辆整体大小。格式： L*W*H
    public String boxSize; // 货厢大小。格式： L*W*H
    public String boxType; // box_type(车厢类型) 字典
    public String goodsType; // 可装载的货物类型：两位:第一位代表形态:1固体，2液体，3气体;第二位:1危化品(特殊商品，上架需要审核)，2普通货物，3生鲜类货物
    public String productTypeIds; //  goodsType =[1|2|3]1时需传
    public String carType; // 车辆类型 字典car_type（车辆类型）
    public Double loadWeight; // 核定载重单位（基础单位*1000)
    public String ownerId; // 车主信息id
    public String driverId1; // 主司机ID
    public String driverId2; // 副司机id
    public Escort escort; // 押运员
    public List<Certificate> certificates; // 车辆证件集合
    public List<Image> images; // 车辆证件集合
}

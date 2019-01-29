package com.lcjian.mmt.data.network.entity;

public class Product {

    public String id;
    public String name;
    public String alias;
    public String imgId; // 商品头像
    public String brand; // 品牌
    public String shortName; // 简称
    public String standard; // 标准。1：国际标准。 2：国家标准。3：行业标准。4：企业标准
    public String model; // 型号
    public String size; // 外形尺寸。格式：长 * 宽* 高
    public String material; // 材质、主要成份
    public String special; // 两位:第一位代表形态:1固体，2液体，3气体;第二位:1危化品(特殊商品，上架需要审核)，2普通货物，3生鲜类货物
    public String property;
    public String description;
    public Store mmtStores; // 仓库
    public Merchant merchant;

    public static class Property {
        public String attrname;
        public String attrvalue;
    }

    public static class Merchant {
        public String id;
        public String name;
    }
}

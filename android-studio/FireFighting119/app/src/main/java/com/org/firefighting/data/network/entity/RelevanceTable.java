package com.org.firefighting.data.network.entity;

public class RelevanceTable {

    public String id;
    public String createBy;
    public String createDate;
    public String remarks;
    public String presentTableId;
    public String presentTableName;
    public String presentTableNameChinese;
    public String relevanceTableId;
    public String relevanceTableName;
    public String relevanceTableNameChinese;
    public String identifier;
    public String relevanceField;
    public String relevanceWay;

    public static class RelevanceField {
        public String source;
        public String target;
    }
}

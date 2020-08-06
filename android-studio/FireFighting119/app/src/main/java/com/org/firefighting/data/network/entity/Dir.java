package com.org.firefighting.data.network.entity;

import java.util.List;

public class Dir {

    public Integer id;
    public String createBy;
    public String createTime;
    public String updateBy;
    public String remarks;
    public Integer pid;
    public String dirCode;
    public String name;
    public String category;
    public String description;
    public String showFlag;
    public String showName;
    public String metricAggFlag;
    public String parentCode;
    public String level;
    public String storagePeriodNum;
    public String storagePeriodUnit;
    public String tableNamePattern;
    public String permission;
    public List<Dir> children;
    public String catalogs;

    // for local
    public boolean first;
    public boolean bold;
}

package com.org.firefighting.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class TaskTable implements Serializable {

    public String id;
    public String name;
    public String type;
    public String description;
    public Long createDate;
    public String createByCode;
    public String createByName;
    public String lastModifiedDate;
    public String lastModifiedByCode;
    public String lastModifiedByName;
    public String origin_merge;
    public String origin_title;
    public String title;
    public String merge;
    public List<List<RtnTitle>> rtnTitle;
    public List<RtnMerge> rtnMerge;
    public String titleIds;
    public String refId;
    public String copyData;

    public static class RtnTitle implements Serializable {

        public String id;
        public String name;
        public Integer maxRow;
        public Integer maxCell;
        public String tableId;
        public Integer row;
        public Integer cell;
        public Integer pos;
        public String type;
        public Boolean requested;
        public String remarks;
        public String range;
        public String handsontableType;
        public String min;
        public String max;
        public String minStr;
        public String maxStr;
        public String valueFormat;
        public Long createDate;
        public String createByCode;
        public String createByName;
        public String refId;
        public List<String> options;

        public String longNameStr;
        public String value;
        public Boolean requestedReal;
    }

    public static class RtnMerge implements Serializable {

        public Integer colspan;
        public Integer col;
        public Boolean removed;
        public Integer rowspan;
        public Integer row;
    }
}

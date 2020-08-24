package com.org.firefighting.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class ResourceEntity implements Serializable {

    public String id;
    public String createBy;
    public String createDate;
    public String remarks;
    public String dataThemeId;
    public String departmentId;
    public String systemId;
    public String dbsId;
    public String dirId;
    public String tableName;
    public String tableCode;
    public String datasourceId;
    public String schemaName;
    public String tableType;
    public String tableCharset;
    public String tableCollation;
    public int dataLength;
    public String tableCreateTime;
    public String tableUpdateTime;
    public String tableComment;
    public String tableVersion;
    public String tableLabel;
    public int ordinalPosition;
    public String grading;
    public String level;
    public String unitName;
    public String unitId;
    public String unitCode;
    public String shareXxzymc;
    public String shareMethod;
    public String shareGxfwbh;
    public String shareGxfwms;
    public String shareXxzyflSsywjz;
    public String shareXxzyflSsys;
    public String shareXxzyzy;
    public String shareXxzybh;
    public String permission;
    public int hot;
    public int star;
    public String astrict;
    public int browses;
    public int collects;
    public int apply;
    public int subscribes;
    public int calls;
    public int connAplications;
    public int appraises;
    public int download;
    public String schema;
    public String dataTheme;
    public String department;
    public String systemName;
    public String searchWord;
    public String collectId;
    public int collectStatus;
    public String collectStatusDescription;
    public String applyId;
    public String applyStatus;
    public String applyTime;
    public String applyStatusDescription;
    public String auditRemarks;
    public String subscribeId;
    public String subscribeStatus;
    public String subscribeStatusDescription;
    public String apply_num;
    public String usage;
    public List<Field> fields;
    public String datas;
    public String statTable;
    public StatInteriorTable statInteriorTable;

    public static class StatInteriorTable implements Serializable {

        public String id;
        public String createBy;
        public String createDate;
        public String remarks;
        public String tableId;
        public int astrict;
        public int browses;
        public int collects;
        public int apply;
        public int subscribes;
        public int calls;
        public int connAplications;
        public int appraises;
        public int download;
        public String schema;
    }

    public static class Field implements Serializable {

        public String id;
        public String createBy;
        public String createDate;
        public String remarks;
        public String dataTableId;
        public String name;
        public String alia;
        public String chineseName;
        public String dataType;
        public String description;
        public String nickDescription;
        public int sort;
        public int status;
        public Integer isStatistics;
        public Integer isDisplay;
        public Integer isSearch;
        public Integer isSort;
    }
}

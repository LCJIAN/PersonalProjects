package com.org.firefighting.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class ServiceEntity implements Serializable {

    public String id;
    public String createBy;
    public String createDate;
    public String remarks;
    public String serviceName;
    public String serviceDeveloper;
    public String type;
    public String description;
    public Integer browses;
    public Integer collects;
    public Integer calls;
    public Integer applyFrequency;
    public Integer connAplications;
    public Integer appraises;
    public String requestHeader;
    public String requestMethod;
    public String contentType;
    public String serviceUrl;
    public String invokeUrl;
    public String invokeName;
    public String params;
    public String status;
    public String tableHash;
    public String astrict;
    public String dirId;
    public Integer hot;
    public String collectId;
    public int collectStatus;
    public String collectStatusDescription;
    public String applyId;
    public int applyStatus;
    public String applyStatusDescription;
    public String subscribeId;
    public String subscribeStatus;
    public String subscribeStatusDescription;

    public String applyTime;
    public String auditRemarks;

    public List<Field> fields;

    public static class Field {
        public Integer paramsType;
        public String name;
        public String dataType;
        public Integer isMust;
        public Integer isDisplay;
        public Integer isSearch;
        public String remarks;
    }
}

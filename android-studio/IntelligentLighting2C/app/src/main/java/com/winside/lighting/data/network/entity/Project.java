package com.winside.lighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class Project {

    @SerializedName("projectId")
    public Long id;
    @SerializedName("projectStatus")
    public Integer status;
    @SerializedName("projectName")
    public String name;
    @SerializedName("projectMulticast")
    public String multicast;
    @SerializedName("buildCount")
    public Integer buildCount;
    @SerializedName("lightTotal")
    public Integer lightTotal;
    @SerializedName("lightFixedTotal")
    public Integer lightFixedTotal;
    @SerializedName("lightOnlineTotal")
    public Integer lightOnlineTotal;
    @SerializedName("sensorTotal")
    public Integer sensorTotal;
    @SerializedName("sensorFixedTotal")
    public Integer sensorFixedTotal;
    @SerializedName("sensorOnlineTotal")
    public Integer sensorOnlineTotal;

}

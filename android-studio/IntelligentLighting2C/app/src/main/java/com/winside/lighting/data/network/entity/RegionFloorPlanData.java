package com.winside.lighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RegionFloorPlanData {

    @SerializedName("id")
    public Long id;
    @SerializedName("name")
    public String name;
    @SerializedName("drawings")
    public String drawings;
    @SerializedName("wh")
    public Double[] wh;
    @SerializedName("gatewayGroup")
    public List<GatewayCoordinate> gatewayCoordinates;
    @SerializedName("point")
    public Double[] point;

    public static class GatewayCoordinate {
        @SerializedName("id")
        public Long id;
        @SerializedName("name")
        public String name;
        @SerializedName("point")
        public Double[] point;

        @SerializedName("deviceId")
        public Long deviceId;
        @SerializedName("deviceStatus")
        public String deviceStatus;
        @SerializedName("type")
        public Integer deviceTypeId;
        @SerializedName("typeName")
        public String deviceTypeName;
        @SerializedName("deviceMac")
        public String deviceMac;

        @SerializedName("deviceGroup")
        public List<DeviceCoordinate> deviceCoordinates;
    }

    public static class DeviceCoordinate {
        @SerializedName("id")
        public Long id;
        @SerializedName("point")
        public Double[] point;

        @SerializedName("deviceId")
        public Long deviceId;
        @SerializedName("type")
        public Integer deviceTypeId;
        @SerializedName("deviceStatus")
        public String deviceStatus;
        @SerializedName("typeName")
        public String deviceTypeName;
    }
}

package com.winside.lighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Device {

    @SerializedName("id")
    public Long id;
    @SerializedName("uuid")
    public String uuid;
    @SerializedName("alias")
    public String alias;
    @SerializedName("status")
    public String status;
    @SerializedName("name")
    public String mac;
    @SerializedName("productId")
    public String productId;
    @SerializedName("productName")
    public String productName;
    @SerializedName("deviceTypeId")
    public Integer typeId;
    @SerializedName("deviceTypeName")
    public String typeName;
    @SerializedName("meshAddr")
    public String meshAddr;
    @SerializedName("snCode")
    public String snCode;
    @SerializedName("rssi")
    public String rssi;
    @SerializedName("realtimePower")
    public String realtimePower;
    @SerializedName("workTimeLong")
    public String workTimeLong;
    @SerializedName("isHaveAnybody")
    public String isHaveAnybody;
    @SerializedName("attribute")
    public Map<String, String> attribute;

    @SerializedName("netKey")
    public String netKey;
    @SerializedName("netkeyIndex")
    public Short netKeyIndex;
    @SerializedName("appKey")
    public String appKey;
    @SerializedName("appkeyIndex")
    public Short appKeyIndex;
    @SerializedName("deviceKey")
    public String deviceKey;
    @SerializedName("ivIndex")
    public Long ivIndex;
    @SerializedName("ivState")
    public Integer ivState;
    @SerializedName("flag")
    public Integer flag;

    @SerializedName("fullUnicast")
    public String fullUniCast;
    @SerializedName("fullMulticast")
    public String fullMultiCast;
}

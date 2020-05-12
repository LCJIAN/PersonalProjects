package com.winside.lighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class Building {

    @SerializedName("buildId")
    public Long id;
    @SerializedName("buildName")
    public String name;
    @SerializedName("buildNumber")
    public String number;
    @SerializedName("buildFloorTotal")
    public Integer floorTotal;
    @SerializedName("buildMulticast")
    public String multicast;
}

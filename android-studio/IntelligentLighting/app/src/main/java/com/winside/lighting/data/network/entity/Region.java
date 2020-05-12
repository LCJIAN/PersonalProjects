package com.winside.lighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class Region {

    @SerializedName("regionId")
    public Long id;
    @SerializedName("regionName")
    public String name;
    @SerializedName("regionNumber")
    public String number;

}

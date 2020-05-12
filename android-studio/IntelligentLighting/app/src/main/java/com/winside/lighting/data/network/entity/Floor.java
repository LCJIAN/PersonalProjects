package com.winside.lighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class Floor {

    @SerializedName("floorId")
    public Long id;
    @SerializedName("floorName")
    public String name;
    @SerializedName("floorNumber")
    public String number;
    @SerializedName("floorIndex")
    public Integer index;
}

package com.winside.lighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

public class SignInInfo {

    @SerializedName("userId")
    public Long userId;
    @SerializedName("token")
    public String token;

}

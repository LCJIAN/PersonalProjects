package com.lcjian.mmt.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SignInInfo {

    @SerializedName("role")
    public List<Role> roles;
    public User user;
    public String token;

}

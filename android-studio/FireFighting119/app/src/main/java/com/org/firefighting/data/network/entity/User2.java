package com.org.firefighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User2 {

    public Long id;
    public String username;
    public String avatar;
    public String email;
    public String phone;
    //    public String dept;
//    public String job;
    public Boolean enabled;
    public Long createTime;
    @SerializedName("realname")
    public String realName;
    public String jobNumber;
    public Long deptid;
    public String deptCode;
    public Integer roleId;
    public String roleName;
    public String dataScope;
    public List<Role> roles;

    public static class Role {

        public Long id;
        public String name;
        public Integer level;
        public String dataScope;
    }
}

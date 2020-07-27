package com.org.firefighting.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User2 {

    public Long id;
    public String username;
    public String avatar;
    public String email;
    public String phone;
    public Boolean enabled;
    public Long createTime;
    @SerializedName("realname")
    public String realName;
    public String jobNumber;
    public Job job;
    public Dept dept;
    public List<Role> roles;

    public static class Role {
        public Long id;
        public String name;
        public Integer level;
        public String dataScope;
    }

    public static class Job {
        public Long id;
        public String name;
    }

    public static class Dept {
        public Long id;
        public String name;
    }
}

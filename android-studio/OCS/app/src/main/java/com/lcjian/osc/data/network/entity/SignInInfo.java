package com.lcjian.osc.data.network.entity;

public class SignInInfo {

    public String state;
    public UserInfo userInfo;
    public DeviceInfo deviceInfo;

    public static class UserInfo {
        public String userID;
        public String userName;
        public String loginName;
        public String timeZone;
        public String warnStr;
        public String warnMsg;
        public String new201710;
        public String test;
        public String fkurl;
        public String isPay;
    }

    public static class DeviceInfo {
        public String deviceID;
        public String deviceName;
        public String sn;
        public String model;
        public String sendCommand;
        public String timeZone;
        public String warnStr;
        public String warnMsg;
        public String new201710;
        public String voice;
        public String test;
        public String fkurl;
        public String isPay;
    }
}

package com.winside.lighting.data.network.entity;

public class DeviceControl {

    public String option;
    public String NetID;
    public Integer unicast;
    public Integer multicast;
    public Data data;

    public static class Data {
        public String opcode;
        public String params;
    }
}

package com.lcjian.cloudlocation.data.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Route {

    public String state;
    @SerializedName("devices")
    public List<Position> list;

    public static class Position {

        public String pt;
        public String lat;
        public String lng;
        public String s;
        public String c;
        public String stop;
        public String stm;
        public String id;
        public String g;
    }
}

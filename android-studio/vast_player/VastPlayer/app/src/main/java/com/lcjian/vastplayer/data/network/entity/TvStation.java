package com.lcjian.vastplayer.data.network.entity;

public class TvStation {

    public String name;
    public String channel;
    public String logo;
    public String type;
    public Program now;

    public static class Program {

        public String name;
        public String time;

    }
}

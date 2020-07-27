package com.org.firefighting.data.network.entity;

import java.util.List;

public class Weather {

    public Data data;

    public static class Data {
        public List<Forecast> forecast;
    }

    public static class Forecast {
        public String fengxiang;
        public String fengli;
        public String high;
        public String type;
        public String low;
        public String date;
    }
}

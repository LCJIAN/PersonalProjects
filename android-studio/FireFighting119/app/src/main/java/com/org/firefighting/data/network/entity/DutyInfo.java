package com.org.firefighting.data.network.entity;

import java.util.List;

public class DutyInfo {

    public List<DutyItem> data;

    public static class DutyItem {
        public String date;
        public String type;
        public String name;
        public String xh;
        public String ssdw;
    }
}

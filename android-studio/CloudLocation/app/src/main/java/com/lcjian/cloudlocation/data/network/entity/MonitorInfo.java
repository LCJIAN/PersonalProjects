package com.lcjian.cloudlocation.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class MonitorInfo {

    public List<MonitorDevice> devices;

    public static class MonitorDevice implements Serializable {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        public String id;
        public String name;
        public String model;
        public String positionTime;
        public String lat;
        public String lng;
        public String speed;
        public String course;
        public String isStop;
        public String stm;
        public String status;
        public String isGPS;

        // 实时跟踪
        public String work;
        public String ICCID;
        public String VIN;

        public String address;
    }
}

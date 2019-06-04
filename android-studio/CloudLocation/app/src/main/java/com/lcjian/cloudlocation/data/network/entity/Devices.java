package com.lcjian.cloudlocation.data.network.entity;

import java.io.Serializable;
import java.util.List;

public class Devices {

    public String state;
    public String nowPage;
    public String resSize;
    public List<Device> arr;

    public static class Device implements Serializable {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        public String id;
        public String name;
        public String sn;
        public String model;
        public String isXm;
        public String sendCommand;
        public String status;
        public String speed;
        public String voice;
    }
}

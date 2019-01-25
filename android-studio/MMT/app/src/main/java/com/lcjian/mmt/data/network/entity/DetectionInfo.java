package com.lcjian.mmt.data.network.entity;

import java.util.Date;
import java.util.List;

public class DetectionInfo {

    public Integer totalCount;
    public List<Item> items;

    public static class Item {
        public Long id;
        public Double totalMass;
        public Double overMass;
        public Double overRateMass;
        public Double axleLength;
        public Integer axleNumber;
        public Double speed;
        public String licensePlate;
        public String laneNumber;
        public Integer directionOff;
        public Boolean isOvering;
        public Boolean isChecking;
        public Double length;
        public Double width;
        public Double height;
        public Date recordTime;

        public List<Checking> checkings;
    }

    public static class Checking {
        public Long id;
        public Double totalMass;
        public Double overMass;
        public Double overRateMass;
        public Long pictureFirst;
        public String licensePlate;
        public Date recordTime;
    }
}

package com.lcjian.mmt.data.network.entity;

import java.util.Date;

public class DetectionRequestData {

    public Long stationId;
    public Integer axleNumber;
    public Double speed;
    public String licensePlate;
    public Date startDate;
    public Date endDate;
    public Double startTotal;
    public Double endTotal;
    public Double startOverRateMass;
    public Double endOverRateMass;
    public String laneNumber;
    public Boolean isOvering;
    public Boolean isChecking;
    public Integer directionOff;
    public String sorting;
    public Integer skipCount;
    public Integer maxResultCount;
}

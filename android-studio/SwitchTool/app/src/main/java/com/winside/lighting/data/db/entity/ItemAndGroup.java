package com.winside.lighting.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class ItemAndGroup {

    @Embedded
    public DeviceSwitchItem item;

    @ColumnInfo(name = "group_name")
    public String groupName;

    @ColumnInfo(name = "group_address")
    public String groupAddress;

    @ColumnInfo(name = "device_name")
    public String deviceName;

    @ColumnInfo(name = "device_address")
    public String deviceAddress;
}

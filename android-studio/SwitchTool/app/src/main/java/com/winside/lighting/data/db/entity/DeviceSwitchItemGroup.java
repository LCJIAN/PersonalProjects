package com.winside.lighting.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "device_switch_item_group", indices = {@Index(value = "time_added", unique = true)})
public class DeviceSwitchItemGroup {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "address")
    public String address;

    // 添加时间
    @ColumnInfo(name = "time_added")
    public Date timeAdded;
}

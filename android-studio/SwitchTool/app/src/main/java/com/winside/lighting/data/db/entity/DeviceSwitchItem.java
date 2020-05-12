package com.winside.lighting.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "device_switch_item",
        indices = {@Index(value = "device_id"),
                @Index(value = "group_id")},
        foreignKeys = {@ForeignKey(entity = Device.class, parentColumns = "id", childColumns = "device_id", onDelete = ForeignKey.CASCADE)
                , @ForeignKey(entity = DeviceSwitchItemGroup.class, parentColumns = "id", childColumns = "group_id", onDelete = ForeignKey.SET_NULL)})
public class DeviceSwitchItem {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "index")
    public Integer index;

    @ColumnInfo(name = "device_id")
    public Long deviceId;

    @ColumnInfo(name = "group_id")
    public Long groupId;
}

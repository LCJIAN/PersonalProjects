package com.lcjian.drinkwater.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * 杯子
 */
@Entity(tableName = "cup")
public class Cup {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    // 杯子容量
    @ColumnInfo(name = "cup_capacity")
    public Double cupCapacity;

    // 添加时间
    @ColumnInfo(name = "time_added")
    public Date timeAdded;

    // 修改时间
    @ColumnInfo(name = "time_modified")
    public Date timeModified;

}

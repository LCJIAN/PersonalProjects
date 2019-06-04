package com.lcjian.drinkwater.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * 喝水记录
 */
@Entity(tableName = "record", indices = {@Index(value = "time_added", unique = true)})
public class Record {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    // 喝水量 1/4,2/4,3/4,4/4倍杯子容量
    @ColumnInfo(name = "intake")
    public Double intake;

    // 喝水用的杯子容量
    @ColumnInfo(name = "cup_capacity")
    public Double cupCapacity;

    // 添加时间
    @ColumnInfo(name = "time_added")
    public Date timeAdded;

    // 修改时间
    @ColumnInfo(name = "time_modified")
    public Date timeModified;

}

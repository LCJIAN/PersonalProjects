package com.lcjian.drinkwater.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 配置
 */
@Entity(tableName = "config")
public class Config {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    // 最小体重
    @ColumnInfo(name = "min_weight")
    public Double minWeight;

    // 最大体重
    @ColumnInfo(name = "max_weight")
    public Double maxWeight;

    // 提醒时间间隔 30,45,60,90
    @ColumnInfo(name = "reminder_intervals")
    public String reminderIntervals;

}

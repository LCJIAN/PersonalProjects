package com.lcjian.drinkwater.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "default_config")
public class DefaultConfig {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "default_gender")
    public Integer defaultGender;

    @ColumnInfo(name = "default_max_weight")
    public Double defaultMaxWeight;

    @ColumnInfo(name = "default_weight")
    public Double defaultWeight;

    @ColumnInfo(name = "default_unit_id")
    public Long defaultUnitId;

    @ColumnInfo(name = "default_wake_up_time")
    public String defaultWakeUpTime;

    @ColumnInfo(name = "default_sleep_time")
    public String defaultSleepTime;
}

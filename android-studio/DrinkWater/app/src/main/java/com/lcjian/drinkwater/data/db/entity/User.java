package com.lcjian.drinkwater.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "user", indices = {@Index(value = "unit_id", unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "gender")
    public Integer gender;

    @ColumnInfo(name = "weight")
    public Double weight;

    @ColumnInfo(name = "unit_id")
    public Long unitId;

    @ColumnInfo(name = "wake_up_time")
    public String wakeUpTime;

    @ColumnInfo(name = "sleep_time")
    public String sleepTime;

}
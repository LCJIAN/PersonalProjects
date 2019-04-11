package com.lcjian.drinkwater.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "config")
public class Config {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "min_weight")
    public Double minWeight;

    @ColumnInfo(name = "max_weight")
    public Double maxWeight;

    @ColumnInfo(name = "reminder_intervals")
    public String reminderIntervals;

}

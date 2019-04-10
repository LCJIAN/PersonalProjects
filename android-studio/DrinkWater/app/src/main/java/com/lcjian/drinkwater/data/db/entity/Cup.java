package com.lcjian.drinkwater.data.db.entity;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cup")
public class Cup {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "cup_capacity")
    public Double cupCapacity;

    @ColumnInfo(name = "time_added")
    public Date timeAdded;

    @ColumnInfo(name = "time_modified")
    public Date timeModified;

}

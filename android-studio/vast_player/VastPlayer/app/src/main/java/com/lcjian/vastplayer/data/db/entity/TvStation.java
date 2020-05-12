package com.lcjian.vastplayer.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tv_station")
public class TvStation {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "source")
    public String source;
}

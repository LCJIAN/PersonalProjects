package com.lcjian.vastplayer.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "search_history")
public class SearchHistory {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "update_time")
    public Date updateTime;
}

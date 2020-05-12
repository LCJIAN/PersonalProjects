package com.lcjian.vastplayer.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "favourite")
public class Favourite implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;


    @PrimaryKey
    @ColumnInfo(name = "subject_id")
    public Long subjectId;

    @ColumnInfo(name = "subject_type")
    public String subjectType;

    @ColumnInfo(name = "create_time")
    public Date createTime;

}

package com.lcjian.vastplayer.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "watch_history", indices = {@Index(value = {"subject_id", "subject_video_id"}, unique = true)})
public class WatchHistory implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "subject_id")
    public Long subjectId;

    @ColumnInfo(name = "subject_type")
    public String subjectType;

    @ColumnInfo(name = "subject_video_id")
    public Long subjectVideoId;

    @ColumnInfo(name = "subject_video_name")
    public String subjectVideoName;

    @ColumnInfo(name = "duration")
    public Long duration;

    @ColumnInfo(name = "watch_time")
    public Long watchTime;

    @ColumnInfo(name = "update_time")
    public Date updateTime;

}

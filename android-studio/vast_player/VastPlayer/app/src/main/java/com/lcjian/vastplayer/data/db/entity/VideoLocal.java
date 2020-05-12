package com.lcjian.vastplayer.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "video_local", indices = {@Index(value = {"data"}, unique = true)})
public class VideoLocal {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "data")
    public String data;

    @ColumnInfo(name = "directory")
    public String directory;

    @ColumnInfo(name = "directory_name")
    public String directoryName;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "title_key")
    public String titleKey;

    @ColumnInfo(name = "size")
    public Long size;

    @ColumnInfo(name = "duration")
    public Long duration;

    @ColumnInfo(name = "width")
    public Long width;

    @ColumnInfo(name = "height")
    public Long height;

    @ColumnInfo(name = "date_added")
    public Date dateAdded;

    @ColumnInfo(name = "date_modified")
    public Date dateModified;

    @ColumnInfo(name = "mime_type")
    public String mimeType;

}

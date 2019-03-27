package com.lcjian.mmt.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "book_history", indices = {@Index(value = "book_id", unique = true)})
public class BookHistory {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @ColumnInfo(name = "book_id")
    public Long bookId;

    @ColumnInfo(name = "book_name")
    public String bookName;

    @ColumnInfo(name = "chapter_index")
    public Long chapterIndex;

    @ColumnInfo(name = "chapter_name")
    public String chapterName;

    @ColumnInfo(name = "chapter_begin")
    public Long chapterBegin;

    @ColumnInfo(name = "chapter_count")
    public Long chapterCount;

    @ColumnInfo(name = "read_time")
    public Long readTime;
}
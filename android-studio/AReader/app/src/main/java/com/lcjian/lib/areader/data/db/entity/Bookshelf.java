package com.lcjian.lib.areader.data.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "bookshelf", indices = {@Index(value = "book_id", unique = true)})
public class Bookshelf {

    //    @PrimaryKey(autoGenerate = true)
//    public Long id;
    @PrimaryKey
    @ColumnInfo(name = "book_id")
    public Long bookId;

    @ColumnInfo(name = "book_name")
    public String bookName;

}
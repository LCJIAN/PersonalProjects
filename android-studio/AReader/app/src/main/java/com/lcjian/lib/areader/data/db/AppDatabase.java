package com.lcjian.lib.areader.data.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.lcjian.lib.areader.data.db.dao.BookHistoryDao;
import com.lcjian.lib.areader.data.db.dao.BookshelfDao;
import com.lcjian.lib.areader.data.db.entity.BookHistory;
import com.lcjian.lib.areader.data.db.entity.Bookshelf;

@Database(entities = {Bookshelf.class, BookHistory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BookshelfDao bookshelfDao();

    public abstract BookHistoryDao bookHistoryDao();
}
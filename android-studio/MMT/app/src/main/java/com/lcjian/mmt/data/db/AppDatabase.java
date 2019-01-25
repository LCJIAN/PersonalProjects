package com.lcjian.mmt.data.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.lcjian.mmt.data.db.dao.BookHistoryDao;
import com.lcjian.mmt.data.db.entity.BookHistory;

@Database(entities = {BookHistory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BookHistoryDao bookHistoryDao();
}
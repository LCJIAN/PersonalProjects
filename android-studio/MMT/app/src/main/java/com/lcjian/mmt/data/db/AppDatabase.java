package com.lcjian.mmt.data.db;

import com.lcjian.mmt.data.db.dao.BookHistoryDao;
import com.lcjian.mmt.data.db.entity.BookHistory;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {BookHistory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BookHistoryDao bookHistoryDao();
}
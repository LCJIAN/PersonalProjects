package com.lcjian.osc.data.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.lcjian.osc.data.db.dao.BookHistoryDao;
import com.lcjian.osc.data.db.entity.BookHistory;

@Database(entities = {BookHistory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BookHistoryDao bookHistoryDao();
}
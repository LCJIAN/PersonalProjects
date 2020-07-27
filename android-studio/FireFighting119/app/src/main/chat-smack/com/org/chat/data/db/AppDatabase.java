package com.org.chat.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.org.chat.data.db.dao.MessageDao;
import com.org.chat.data.db.entity.Message;

@Database(entities = {Message.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract MessageDao messageDao();
}

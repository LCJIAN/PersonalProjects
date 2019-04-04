package com.lcjian.drinkwater.data.db;

import com.lcjian.drinkwater.data.db.dao.DefaultConfigDao;
import com.lcjian.drinkwater.data.db.dao.UnitDao;
import com.lcjian.drinkwater.data.db.dao.UserDao;
import com.lcjian.drinkwater.data.db.entity.DefaultConfig;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.data.db.entity.User;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Unit.class, DefaultConfig.class, User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UnitDao unitDao();

    public abstract DefaultConfigDao defaultConfigDao();

    public abstract UserDao userDao();
}
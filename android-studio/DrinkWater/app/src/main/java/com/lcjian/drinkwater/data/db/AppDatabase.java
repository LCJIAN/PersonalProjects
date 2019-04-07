package com.lcjian.drinkwater.data.db;

import com.lcjian.drinkwater.data.db.dao.DefaultConfigDao;
import com.lcjian.drinkwater.data.db.dao.SettingDao;
import com.lcjian.drinkwater.data.db.dao.UnitDao;
import com.lcjian.drinkwater.data.db.entity.DefaultConfig;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.data.db.entity.Unit;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Unit.class, DefaultConfig.class, Setting.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UnitDao unitDao();

    public abstract DefaultConfigDao defaultConfigDao();

    public abstract SettingDao userDao();
}
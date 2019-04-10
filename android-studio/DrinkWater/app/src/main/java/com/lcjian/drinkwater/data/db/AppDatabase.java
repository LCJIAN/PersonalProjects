package com.lcjian.drinkwater.data.db;

import com.lcjian.drinkwater.data.db.dao.ConfigDao;
import com.lcjian.drinkwater.data.db.dao.CupDao;
import com.lcjian.drinkwater.data.db.dao.RecordDao;
import com.lcjian.drinkwater.data.db.dao.SettingDao;
import com.lcjian.drinkwater.data.db.dao.UnitDao;
import com.lcjian.drinkwater.data.db.entity.Config;
import com.lcjian.drinkwater.data.db.entity.Cup;
import com.lcjian.drinkwater.data.db.entity.Record;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.data.db.entity.Unit;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Unit.class, Config.class, Setting.class, Record.class, Cup.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract UnitDao unitDao();

    public abstract ConfigDao configDao();

    public abstract SettingDao settingDao();

    public abstract RecordDao recordDao();

    public abstract CupDao cupDao();
}
package com.winside.lighting.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.winside.lighting.data.db.dao.DeviceDao;
import com.winside.lighting.data.db.dao.DeviceSwitchItemDao;
import com.winside.lighting.data.db.dao.DeviceSwitchItemGroupDao;
import com.winside.lighting.data.db.entity.Device;
import com.winside.lighting.data.db.entity.DeviceSwitchItem;
import com.winside.lighting.data.db.entity.DeviceSwitchItemGroup;

@Database(entities = {Device.class, DeviceSwitchItemGroup.class, DeviceSwitchItem.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract DeviceDao deviceDao();

    public abstract DeviceSwitchItemDao deviceSwitchItemDao();

    public abstract DeviceSwitchItemGroupDao deviceSwitchItemGroupDao();
}

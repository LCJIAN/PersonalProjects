package com.winside.lighting.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.winside.lighting.data.db.entity.DeviceSwitchItemGroup;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface DeviceSwitchItemGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceSwitchItemGroup... groups);

    @Delete
    void delete(DeviceSwitchItemGroup... groups);

    @Update
    void update(DeviceSwitchItemGroup... groups);

    @Query("SELECT * FROM device_switch_item_group")
    List<DeviceSwitchItemGroup> getAll();

    @Query("SELECT * FROM device_switch_item_group")
    Flowable<List<DeviceSwitchItemGroup>> getAllRx();
}

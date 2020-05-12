package com.winside.lighting.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.winside.lighting.data.db.entity.DeviceSwitchItem;
import com.winside.lighting.data.db.entity.ItemAndGroup;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface DeviceSwitchItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceSwitchItem... items);

    @Delete
    void delete(DeviceSwitchItem... items);

    @Update
    void update(DeviceSwitchItem... items);

    @Query("SELECT * FROM device_switch_item")
    List<DeviceSwitchItem> getAll();

    @Query("SELECT * FROM device_switch_item")
    Flowable<List<DeviceSwitchItem>> getAllRx();

    @Query("SELECT d.*, g.name AS group_name " +
            "FROM device_switch_item d LEFT OUTER JOIN device_switch_item_group g ON d.group_id == g.id " +
            "WHERE d.device_id = :deviceId ")
    Flowable<List<ItemAndGroup>> getAllByDeviceIdRx(Long deviceId);

    @Query("SELECT d.*, g.name AS group_name, g.address AS group_address, de.name AS device_name, de.address AS device_address " +
            "FROM device_switch_item d " +
            "INNER JOIN device_switch_item_group g ON d.group_id == g.id " +
            "INNER JOIN device de ON d.device_id == de.id " +
            "WHERE d.group_id = :groupId ")
    Flowable<List<ItemAndGroup>> getAllByGroupIdRx(Long groupId);
}

package com.winside.lighting.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.winside.lighting.data.db.entity.Device;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long[] insert(Device... devices);

    @Delete
    void delete(Device... devices);

    @Update
    void update(Device... devices);

    @Query("SELECT * FROM device")
    List<Device> getAll();

    @Query("SELECT * FROM device")
    Flowable<List<Device>> getAllRx();
}

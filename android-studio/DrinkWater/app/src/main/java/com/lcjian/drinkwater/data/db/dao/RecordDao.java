package com.lcjian.drinkwater.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lcjian.drinkwater.data.db.entity.Record;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Record... records);

    @Delete
    void delete(Record... records);

    @Update
    void update(Record... records);

    @Query("SELECT * FROM record")
    List<Record> getAllSync();

    @Query("SELECT * FROM record")
    Flowable<List<Record>> getAllAsync();

    @Query("SELECT * FROM record WHERE id = :id")
    Flowable<List<Record>> getAllAsyncById(Long id);

    // 获取第一条喝水记录
    @Query("SELECT * FROM record ORDER BY time_added ASC LIMIT 0,1")
    Flowable<List<Record>> getFirstAsync();

    // 获取最新一条喝水记录
    @Query("SELECT * FROM record ORDER BY time_added DESC LIMIT 0,1")
    Flowable<List<Record>> getLatestAsync();

    // 获取时间间隔内的喝水记录
    @Query("SELECT * FROM record WHERE time_added >= :startTime AND time_added < :endTime ORDER BY time_added DESC")
    Flowable<List<Record>> getAllAsyncByTime(Date startTime, Date endTime);

    // 获取时间间隔内的喝水记录
    @Query("SELECT * FROM record WHERE time_added >= :startTime AND time_added < :endTime ORDER BY time_added DESC")
    List<Record> getAllSyncByTime(Date startTime, Date endTime);
}
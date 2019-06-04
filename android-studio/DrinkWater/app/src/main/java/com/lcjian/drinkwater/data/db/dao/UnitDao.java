package com.lcjian.drinkwater.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lcjian.drinkwater.data.db.entity.Unit;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface UnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Unit... units);

    @Delete
    void delete(Unit... units);

    @Update
    void update(Unit... units);

    @Query("SELECT * FROM unit")
    List<Unit> getAllSync();

    @Query("SELECT * FROM unit WHERE name LIKE :name")
    List<Unit> getAllSyncByName(String name);

    // 获取当前单位，结果只有一条
    @Query("SELECT u.* FROM unit u INNER JOIN setting s ON s.unit_id = u.id")
    List<Unit> getCurrentUnitSync();

    @Query("SELECT * FROM unit")
    Flowable<List<Unit>> getAllAsync();

    // 获取当前单位，结果只有一条
    @Query("SELECT u.* FROM unit u INNER JOIN setting s ON s.unit_id = u.id")
    Flowable<List<Unit>> getCurrentUnitAsync();
}

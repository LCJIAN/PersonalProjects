package com.lcjian.drinkwater.data.db.dao;

import com.lcjian.drinkwater.data.db.entity.Unit;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
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

    @Query("SELECT * FROM unit INNER JOIN setting ON setting.unit_id = unit.id")
    List<Unit> getCurrentUnitSync();

    @Query("SELECT * FROM unit")
    Flowable<List<Unit>> getAllAsync();

    @Query("SELECT * FROM unit INNER JOIN setting ON setting.unit_id = unit.id")
    Flowable<List<Unit>> getCurrentUnitAsync();
}

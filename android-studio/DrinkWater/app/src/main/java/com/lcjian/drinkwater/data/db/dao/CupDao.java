package com.lcjian.drinkwater.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lcjian.drinkwater.data.db.entity.Cup;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface CupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Cup... cups);

    @Delete
    void delete(Cup... cups);

    @Update
    void update(Cup... cups);

    @Query("SELECT * FROM cup")
    List<Cup> getAllSync();

    @Query("SELECT * FROM cup")
    Flowable<List<Cup>> getAllAsync();

    @Query("SELECT * FROM cup WHERE cup_capacity = :cupCapacity")
    List<Cup> getAllSyncByCapacity(double cupCapacity);

    // 获取当前杯子
    @Query("SELECT c.* FROM cup c INNER JOIN setting s ON s.cup_id = c.id")
    Flowable<List<Cup>> getCurrentCupAsync();
}
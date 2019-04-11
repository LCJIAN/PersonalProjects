package com.lcjian.drinkwater.data.db.dao;

import com.lcjian.drinkwater.data.db.entity.Cup;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
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

    @Query("SELECT * FROM cup INNER JOIN setting ON setting.cup_id = cup.id")
    List<Cup> getCurrentCupSync();

    @Query("SELECT * FROM cup INNER JOIN setting ON setting.cup_id = cup.id")
    Flowable<List<Cup>> getCurrentCupAsync();
}
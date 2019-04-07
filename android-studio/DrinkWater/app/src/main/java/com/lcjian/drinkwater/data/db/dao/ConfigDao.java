package com.lcjian.drinkwater.data.db.dao;

import com.lcjian.drinkwater.data.db.entity.Config;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Flowable;

@Dao
public interface ConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Config... configs);

    @Delete
    void delete(Config... configs);

    @Update
    void update(Config... configs);

    @Query("SELECT * FROM config")
    List<Config> getAllSync();

    @Query("SELECT * FROM config")
    Flowable<List<Config>> getAllAsync();
}

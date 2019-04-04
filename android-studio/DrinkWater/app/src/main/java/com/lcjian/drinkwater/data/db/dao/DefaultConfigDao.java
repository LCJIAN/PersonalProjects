package com.lcjian.drinkwater.data.db.dao;

import com.lcjian.drinkwater.data.db.entity.DefaultConfig;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Flowable;

@Dao
public interface DefaultConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DefaultConfig... defaultConfigs);

    @Delete
    void delete(DefaultConfig... defaultConfigs);

    @Update
    void update(DefaultConfig... defaultConfigs);

    @Query("SELECT * FROM default_config")
    List<DefaultConfig> getAllSync();

    @Query("SELECT * FROM default_config")
    Flowable<List<DefaultConfig>> getAllAsync();
}

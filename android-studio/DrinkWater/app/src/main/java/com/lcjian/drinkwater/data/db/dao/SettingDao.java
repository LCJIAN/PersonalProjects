package com.lcjian.drinkwater.data.db.dao;

import com.lcjian.drinkwater.data.db.entity.Setting;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Flowable;

@Dao
public interface SettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Setting... settings);

    @Delete
    void delete(Setting... settings);

    @Update
    void update(Setting... settings);

    @Query("SELECT * FROM setting")
    List<Setting> getAllSync();

    @Query("SELECT * FROM setting")
    Flowable<List<Setting>> getAllAsync();
}

package com.lcjian.vastplayer.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lcjian.vastplayer.data.db.entity.TvStation;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface TvStationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TvStation... tvStations);

    @Delete
    void delete(TvStation... tvStations);

    @Update
    void update(TvStation... tvStations);

    @Query("SELECT * FROM tv_station")
    List<TvStation> getAllSync();

    @Query("SELECT * FROM tv_station")
    Flowable<List<TvStation>> getAllAsync();

    @Query("SELECT count(*) FROM tv_station")
    Integer getCountSync();

    @Query("SELECT * FROM tv_station LIMIT :pageSize OFFSET :offset")
    List<TvStation> getPageSync(Integer pageSize, Integer offset);

}

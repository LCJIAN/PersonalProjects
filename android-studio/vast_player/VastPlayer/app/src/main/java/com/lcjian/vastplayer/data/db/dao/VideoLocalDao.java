package com.lcjian.vastplayer.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lcjian.vastplayer.data.db.entity.VideoLocal;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface VideoLocalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(VideoLocal... localVideos);

    @Delete
    void delete(VideoLocal... localVideos);

    @Update
    void update(VideoLocal... localVideos);

    @Query("SELECT * FROM video_local")
    List<VideoLocal> getAllSync();

    @Query("SELECT * FROM video_local")
    Flowable<List<VideoLocal>> getAllAsync();

    @Query("SELECT * FROM video_local WHERE data = :data")
    List<VideoLocal> getByDataSync(String data);
}

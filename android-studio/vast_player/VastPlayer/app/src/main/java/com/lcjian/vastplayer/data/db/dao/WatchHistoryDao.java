package com.lcjian.vastplayer.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lcjian.vastplayer.data.db.entity.WatchHistory;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface WatchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WatchHistory... watchHistories);

    @Delete
    void delete(WatchHistory... watchHistories);

    @Update
    void update(WatchHistory... watchHistories);

    @Query("SELECT * FROM watch_history")
    List<WatchHistory> getAllSync();

    @Query("SELECT * FROM watch_history")
    Flowable<List<WatchHistory>> getAllAsync();

    @Query("SELECT count(*) FROM watch_history WHERE subject_type = :subjectType")
    Integer getCountByTypeSync(String subjectType);

    @Query("SELECT * FROM watch_history WHERE subject_type = :subjectType ORDER BY update_time DESC LIMIT :pageSize OFFSET :offset")
    List<WatchHistory> getPageByTypeSync(String subjectType, Integer pageSize, Integer offset);

    @Query("SELECT * FROM watch_history WHERE subject_id = :subjectId AND subject_video_id = :subjectVideoId")
    List<WatchHistory> getByIdSync(Long subjectId, Long subjectVideoId);

}

package com.lcjian.vastplayer.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lcjian.vastplayer.data.db.entity.SearchHistory;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SearchHistory... searchHistories);

    @Delete
    void delete(SearchHistory... searchHistories);

    @Update
    void update(SearchHistory... searchHistories);

    @Query("SELECT * FROM search_history")
    List<SearchHistory> getAllSync();

    @Query("SELECT * FROM search_history")
    Flowable<List<SearchHistory>> getAllAsync();

    @Query("SELECT * FROM search_history ORDER BY update_time DESC")
    List<SearchHistory> getOrderByTimeSync();

    @Query("SELECT * FROM search_history WHERE text LIKE :keyword ORDER BY update_time DESC")
    List<SearchHistory> getByTextLikeSync(String keyword);

    @Query("SELECT * FROM search_history WHERE text = :keyword")
    List<SearchHistory> getByTextSync(String keyword);
}

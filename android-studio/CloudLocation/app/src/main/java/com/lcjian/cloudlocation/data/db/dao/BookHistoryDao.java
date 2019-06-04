package com.lcjian.cloudlocation.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.lcjian.cloudlocation.data.db.entity.BookHistory;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface BookHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(BookHistory... bookHistories);

    @Delete
    void delete(BookHistory... bookHistories);

    @Update
    void update(BookHistory... bookHistories);

    @Query("SELECT * FROM book_history")
    List<BookHistory> getAllSync();

    @Query("SELECT * FROM book_history")
    Flowable<List<BookHistory>> getAllAsync();
}

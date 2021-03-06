package com.lcjian.cloudlocation.data.db.dao;

import com.lcjian.cloudlocation.data.db.entity.BookHistory;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
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

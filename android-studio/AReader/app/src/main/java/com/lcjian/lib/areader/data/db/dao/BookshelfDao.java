package com.lcjian.lib.areader.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.lcjian.lib.areader.data.db.entity.Bookshelf;

import java.util.List;

@Dao
public interface BookshelfDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Bookshelf... bookshelves);

    @Delete
    void delete(Bookshelf... bookshelves);

    @Update
    void update(Bookshelf... bookshelves);

    @Query("SELECT * FROM bookshelf")
    List<Bookshelf> getAll();

//    @Query("SELECT * FROM bookshelf")
//    Observable<List<Bookshelf>> loadAll();

    @Query("SELECT * FROM bookshelf WHERE book_id IN (:bookIds)")
    List<Bookshelf> getBookshelvesByBookIds(List<Long> bookIds);
}

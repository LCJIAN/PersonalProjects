package com.lcjian.drinkwater.data.db.dao;

import com.lcjian.drinkwater.data.db.entity.User;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Flowable;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User... users);

    @Delete
    void delete(User... users);

    @Update
    void update(User... users);

    @Query("SELECT * FROM user")
    List<User> getAllSync();

    @Query("SELECT * FROM user")
    Flowable<List<User>> getAllAsync();
}

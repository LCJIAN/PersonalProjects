package com.lcjian.vastplayer.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lcjian.vastplayer.data.db.entity.Favourite;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface FavouriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Favourite... favourites);

    @Delete
    void delete(Favourite... favourites);

    @Update
    void update(Favourite... favourites);

    @Query("SELECT * FROM favourite")
    List<Favourite> getAllSync();

    @Query("SELECT * FROM favourite")
    Flowable<List<Favourite>> getAllAsync();

    @Query("SELECT count(*) FROM favourite WHERE subject_type = :subjectType")
    Integer getCountByTypeSync(String subjectType);

    @Query("SELECT * FROM favourite WHERE subject_type = :subjectType ORDER BY create_time DESC LIMIT :pageSize OFFSET :offset")
    List<Favourite> getPageByTypeSync(String subjectType, Integer pageSize, Integer offset);

    @Query("SELECT * FROM favourite WHERE subject_id = :subjectId and subject_type = :subjectType")
    Flowable<List<Favourite>> getByIdAndTypeAsync(Long subjectId, String subjectType);

}

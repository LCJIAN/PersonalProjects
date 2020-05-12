package com.lcjian.vastplayer.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lcjian.vastplayer.data.db.dao.FavouriteDao;
import com.lcjian.vastplayer.data.db.dao.SearchHistoryDao;
import com.lcjian.vastplayer.data.db.dao.TvStationDao;
import com.lcjian.vastplayer.data.db.dao.VideoLocalDao;
import com.lcjian.vastplayer.data.db.dao.WatchHistoryDao;
import com.lcjian.vastplayer.data.db.entity.Favourite;
import com.lcjian.vastplayer.data.db.entity.SearchHistory;
import com.lcjian.vastplayer.data.db.entity.TvStation;
import com.lcjian.vastplayer.data.db.entity.VideoLocal;
import com.lcjian.vastplayer.data.db.entity.WatchHistory;

@Database(entities = {
        Favourite.class,
        SearchHistory.class,
        TvStation.class,
        VideoLocal.class,
        WatchHistory.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract FavouriteDao favouriteDao();

    public abstract SearchHistoryDao searchHistoryDao();

    public abstract TvStationDao tvStationDao();

    public abstract VideoLocalDao videoLocalDao();

    public abstract WatchHistoryDao watchHistoryDao();
}
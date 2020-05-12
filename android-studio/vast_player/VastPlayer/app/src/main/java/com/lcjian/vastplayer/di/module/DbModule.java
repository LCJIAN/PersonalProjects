package com.lcjian.vastplayer.di.module;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.lcjian.vastplayer.data.db.AppDatabase;
import com.lcjian.vastplayer.di.scope.ApplicationScope;

import dagger.Module;
import dagger.Provides;

@Module
public class DbModule {

    @Provides
    @NonNull
    @ApplicationScope
    public AppDatabase provideAppDatabase(@NonNull Application application) {
        return Room.databaseBuilder(application, AppDatabase.class, "vast-player")
                .allowMainThreadQueries()
                .build();
    }
}

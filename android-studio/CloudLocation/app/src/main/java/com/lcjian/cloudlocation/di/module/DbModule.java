package com.lcjian.cloudlocation.di.module;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.support.annotation.NonNull;

import com.lcjian.cloudlocation.data.db.AppDatabase;
import com.lcjian.cloudlocation.di.scope.ApplicationScope;

import dagger.Module;
import dagger.Provides;

@Module
public class DbModule {

    @Provides
    @NonNull
    @ApplicationScope
    public AppDatabase provideAppDatabase(@NonNull Application application) {
        return Room.databaseBuilder(application, AppDatabase.class, "cloud-location")
                .allowMainThreadQueries()
                .build();
    }
}

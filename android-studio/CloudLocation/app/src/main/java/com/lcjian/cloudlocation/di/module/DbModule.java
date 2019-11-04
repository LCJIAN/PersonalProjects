package com.lcjian.cloudlocation.di.module;

import android.app.Application;

import com.lcjian.cloudlocation.data.db.AppDatabase;
import com.lcjian.cloudlocation.di.scope.ApplicationScope;

import androidx.annotation.NonNull;
import androidx.room.Room;
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

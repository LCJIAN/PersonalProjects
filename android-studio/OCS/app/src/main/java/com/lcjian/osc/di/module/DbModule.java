package com.lcjian.osc.di.module;

import android.app.Application;
import androidx.room.Room;
import androidx.annotation.NonNull;

import com.lcjian.osc.data.db.AppDatabase;
import com.lcjian.osc.di.scope.ApplicationScope;

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

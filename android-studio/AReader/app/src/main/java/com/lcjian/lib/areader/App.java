package com.lcjian.lib.areader;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.support.v7.app.AppCompatDelegate;

import com.lcjian.lib.areader.data.db.AppDatabase;
import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

public class App extends Application {

    private static App INSTANCE;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private AppDatabase mAppDatabase;

    public static App getInstance() {
        return INSTANCE;
    }

    public AppDatabase getDataBase() {
        return mAppDatabase;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        mAppDatabase = Room.databaseBuilder(
                getApplicationContext(), AppDatabase.class, "a-reader")
                .allowMainThreadQueries()
                .build();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        LeakCanary.install(this);
    }
}

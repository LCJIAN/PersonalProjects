package com.winside.lighting;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.room.Room;

import com.winside.lighting.data.db.AppDatabase;

import io.reactivex.plugins.RxJavaPlugins;
import timber.log.Timber;

public class App extends Application {

    private static App INSTANCE;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static App getInstance() {
        return INSTANCE;
    }

    private AppDatabase mAppDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        mAppDatabase = Room.databaseBuilder(this, AppDatabase.class, "switch-tool")
                .allowMainThreadQueries()
                .build();
        RxJavaPlugins.setErrorHandler(Timber::e);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public AppDatabase getAppDatabase() {
        return mAppDatabase;
    }
}

package com.winside.lighting;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

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

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        RxJavaPlugins.setErrorHandler(Timber::e);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}

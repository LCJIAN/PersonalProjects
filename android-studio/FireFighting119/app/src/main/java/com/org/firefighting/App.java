package com.org.firefighting;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

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

        UMConfigure.init(this, Constants.U_KEY, Constants.U_CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ErrorTree());
        }

        RxJavaPlugins.setErrorHandler(Timber::e);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}

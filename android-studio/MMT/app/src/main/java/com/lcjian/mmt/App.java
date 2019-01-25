package com.lcjian.mmt;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.lcjian.mmt.di.component.AppComponent;
import com.lcjian.mmt.di.component.DaggerAppComponent;
import com.lcjian.mmt.di.module.AppModule;
import com.lcjian.mmt.di.module.DbModule;
import com.lcjian.mmt.di.module.RestAPIModule;
import com.lcjian.mmt.di.module.RxBusModule;
import com.lcjian.mmt.di.module.SharedPreferenceModule;
import com.squareup.leakcanary.LeakCanary;

import io.reactivex.plugins.RxJavaPlugins;
import timber.log.Timber;

public class App extends Application {

    private static App INSTANCE;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private AppComponent mAppComponent;

    public static App getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .dbModule(new DbModule())
                .restAPIModule(new RestAPIModule())
                .rxBusModule(new RxBusModule())
                .sharedPreferenceModule(new SharedPreferenceModule())
                .build();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        LeakCanary.install(this);

        RxJavaPlugins.setErrorHandler(Timber::e);
    }

    public AppComponent appComponent() {
        return mAppComponent;
    }
}

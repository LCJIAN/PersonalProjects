package com.lcjian.osc;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.lcjian.osc.di.component.AppComponent;
import com.lcjian.osc.di.component.DaggerAppComponent;
import com.lcjian.osc.di.module.AppModule;
import com.lcjian.osc.di.module.DbModule;
import com.lcjian.osc.di.module.RestAPIModule;
import com.lcjian.osc.di.module.RxBusModule;
import com.lcjian.osc.di.module.SharedPreferenceModule;
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

package com.lcjian.drinkwater;

import android.app.Application;

import com.lcjian.drinkwater.di.component.AppComponent;
import com.lcjian.drinkwater.di.component.DaggerAppComponent;
import com.lcjian.drinkwater.di.module.AppModule;
import com.lcjian.drinkwater.di.module.DbModule;
import com.lcjian.drinkwater.di.module.RestAPIModule;
import com.lcjian.drinkwater.di.module.RxBusModule;
import com.lcjian.drinkwater.di.module.SharedPreferenceModule;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import androidx.appcompat.app.AppCompatDelegate;
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

        UMConfigure.init(this, Constants.U_KEY, Constants.U_CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ErrorTree());
        }
        LeakCanary.install(this);

        RxJavaPlugins.setErrorHandler(Timber::e);
    }

    public AppComponent appComponent() {
        return mAppComponent;
    }

}

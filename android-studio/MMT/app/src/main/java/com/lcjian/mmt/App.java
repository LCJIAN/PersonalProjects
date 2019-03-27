package com.lcjian.mmt;

import android.app.Application;
import android.content.Context;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.lcjian.mmt.di.component.AppComponent;
import com.lcjian.mmt.di.component.DaggerAppComponent;
import com.lcjian.mmt.di.module.AppModule;
import com.lcjian.mmt.di.module.DbModule;
import com.lcjian.mmt.di.module.RestAPIModule;
import com.lcjian.mmt.di.module.RxBusModule;
import com.lcjian.mmt.di.module.SharedPreferenceModule;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import cn.jpush.android.api.JPushInterface;
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

        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        JPushInterface.init(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            JPushInterface.setDebugMode(true);
        } else {
            Timber.plant(new ErrorTree());
        }
        LeakCanary.install(this);

        RxJavaPlugins.setErrorHandler(Timber::e);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public AppComponent appComponent() {
        return mAppComponent;
    }
}

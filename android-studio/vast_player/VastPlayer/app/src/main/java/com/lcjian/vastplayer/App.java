package com.lcjian.vastplayer;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

import com.lcjian.vastplayer.di.component.AppComponent;
import com.lcjian.vastplayer.di.component.DaggerAppComponent;
import com.lcjian.vastplayer.di.module.AppModule;
import com.lcjian.vastplayer.di.module.DbModule;
import com.lcjian.vastplayer.di.module.RestAPIModule;
import com.lcjian.vastplayer.di.module.RxBusModule;
import com.lcjian.vastplayer.di.module.SharedPreferenceModule;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

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

        UMConfigure.init(this, Constants.U_KEY, BuildConfig.U_CHANNEL, UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        Config.DEBUG = true;
        PlatformConfig.setQQZone(Constants.QQ_ID, Constants.QQ_KEY);
        PlatformConfig.setWeixin(Constants.WE_CHAT_ID, Constants.WE_CHAT_SECRET);
        PlatformConfig.setSinaWeibo(Constants.WEIBO_KEY, Constants.WEIBO_SECRET, "");
        UMShareAPI.get(this);

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

    public AppComponent appComponent() {
        return mAppComponent;
    }

}

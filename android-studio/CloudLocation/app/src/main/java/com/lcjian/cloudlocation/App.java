package com.lcjian.cloudlocation;

import android.app.Application;
import android.content.res.Configuration;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.franmontiel.localechanger.LocaleChanger;
import com.franmontiel.localechanger.LocalePreference;
import com.franmontiel.localechanger.matcher.ClosestMatchingAlgorithm;
import com.franmontiel.localechanger.matcher.LanguageMatchingAlgorithm;
import com.lcjian.cloudlocation.di.component.AppComponent;
import com.lcjian.cloudlocation.di.component.DaggerAppComponent;
import com.lcjian.cloudlocation.di.module.AppModule;
import com.lcjian.cloudlocation.di.module.DbModule;
import com.lcjian.cloudlocation.di.module.RestAPIModule;
import com.lcjian.cloudlocation.di.module.RxBusModule;
import com.lcjian.cloudlocation.di.module.SharedPreferenceModule;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

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

        LocaleChanger.initialize(getApplicationContext(),
                Arrays.asList(Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH, new Locale("es")),
                new ClosestMatchingAlgorithm(),
                LocalePreference.PreferSystemLocale);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ErrorTree());
        }
        LeakCanary.install(this);

        RxJavaPlugins.setErrorHandler(Timber::e);

        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleChanger.onConfigurationChanged();
    }

    public AppComponent appComponent() {
        return mAppComponent;
    }
}

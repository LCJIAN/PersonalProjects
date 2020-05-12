package com.lcjian.vastplayer.di.module;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.lcjian.lib.content.ObscuredSharedPreferences;
import com.lcjian.vastplayer.di.scope.ApplicationScope;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class SharedPreferenceModule {

    @Provides
    @NonNull
    @ApplicationScope
    public
    @Named("user_info")
    SharedPreferences provideUserInfoSharedPreferences(@NonNull Application application) {
        return new ObscuredSharedPreferences(application, application.getSharedPreferences("user_info", Context.MODE_PRIVATE));
    }

    @Provides
    @NonNull
    @ApplicationScope
    public
    @Named("setting")
    SharedPreferences provideSettingSharedPreferences(@NonNull Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }
}

package com.lcjian.cloudlocation.di.module;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.lcjian.cloudlocation.di.scope.ApplicationScope;
import com.lcjian.cloudlocation.util.ObscuredSharedPreferences;

import javax.inject.Named;

import androidx.annotation.NonNull;
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

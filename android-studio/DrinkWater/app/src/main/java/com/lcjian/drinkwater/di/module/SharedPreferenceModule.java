package com.lcjian.drinkwater.di.module;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.lcjian.drinkwater.di.scope.ApplicationScope;

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
        return application.getSharedPreferences("user_info", Context.MODE_PRIVATE);
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

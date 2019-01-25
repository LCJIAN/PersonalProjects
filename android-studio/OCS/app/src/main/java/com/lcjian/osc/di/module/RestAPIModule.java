package com.lcjian.osc.di.module;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.lcjian.osc.data.network.RestAPI;
import com.lcjian.osc.di.scope.ApplicationScope;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class RestAPIModule {

    @Provides
    @NonNull
    @ApplicationScope
    public RestAPI provideRestAPI(@Named("user_info") SharedPreferences userInfoSp) {
        return new RestAPI(userInfoSp);
    }
}

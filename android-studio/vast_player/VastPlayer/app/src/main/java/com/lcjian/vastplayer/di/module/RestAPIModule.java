package com.lcjian.vastplayer.di.module;

import androidx.annotation.NonNull;

import com.lcjian.vastplayer.data.network.RestAPI;
import com.lcjian.vastplayer.di.scope.ApplicationScope;

import dagger.Module;
import dagger.Provides;

@Module
public class RestAPIModule {

    @Provides
    @NonNull
    @ApplicationScope
    public RestAPI provideRestAPI() {
        return new RestAPI();
    }
}

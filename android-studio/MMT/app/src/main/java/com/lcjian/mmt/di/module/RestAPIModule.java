package com.lcjian.mmt.di.module;

import android.support.annotation.NonNull;

import com.lcjian.mmt.data.network.RestAPI;
import com.lcjian.mmt.di.scope.ApplicationScope;

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

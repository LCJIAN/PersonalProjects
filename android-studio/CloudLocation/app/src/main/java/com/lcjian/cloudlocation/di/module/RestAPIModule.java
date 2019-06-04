package com.lcjian.cloudlocation.di.module;

import android.support.annotation.NonNull;

import com.lcjian.cloudlocation.data.network.RestAPI;
import com.lcjian.cloudlocation.di.scope.ApplicationScope;

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

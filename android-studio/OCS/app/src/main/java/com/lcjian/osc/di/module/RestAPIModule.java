package com.lcjian.osc.di.module;

import android.support.annotation.NonNull;

import com.lcjian.osc.data.network.RestAPI;
import com.lcjian.osc.di.scope.ApplicationScope;

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

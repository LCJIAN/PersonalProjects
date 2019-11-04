package com.lcjian.cloudlocation.di.module;

import com.lcjian.cloudlocation.data.network.RestAPI;
import com.lcjian.cloudlocation.di.scope.ApplicationScope;

import androidx.annotation.NonNull;
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

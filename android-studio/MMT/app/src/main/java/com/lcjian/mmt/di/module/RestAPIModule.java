package com.lcjian.mmt.di.module;

import com.lcjian.mmt.data.network.RestAPI;
import com.lcjian.mmt.di.scope.ApplicationScope;

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

package com.lcjian.drinkwater.di.module;

import com.lcjian.drinkwater.data.network.RestAPI;
import com.lcjian.drinkwater.di.scope.ApplicationScope;

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

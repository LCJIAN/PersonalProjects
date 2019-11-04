package com.lcjian.cloudlocation.di.module;

import com.lcjian.cloudlocation.RxBus;
import com.lcjian.cloudlocation.di.scope.ApplicationScope;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;

@Module
public class RxBusModule {

    @Provides
    @NonNull
    @ApplicationScope
    public RxBus provideRxBus() {
        return new RxBus();
    }
}

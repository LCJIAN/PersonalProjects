package com.lcjian.drinkwater.di.module;

import com.lcjian.drinkwater.RxBus;
import com.lcjian.drinkwater.di.scope.ApplicationScope;

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

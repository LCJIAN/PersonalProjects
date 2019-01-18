package com.lcjian.osc.di.module;

import android.support.annotation.NonNull;

import com.lcjian.osc.RxBus;
import com.lcjian.osc.di.scope.ApplicationScope;

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

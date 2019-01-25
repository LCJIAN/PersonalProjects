package com.lcjian.mmt.di.module;

import android.support.annotation.NonNull;

import com.lcjian.mmt.RxBus;
import com.lcjian.mmt.di.scope.ApplicationScope;

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

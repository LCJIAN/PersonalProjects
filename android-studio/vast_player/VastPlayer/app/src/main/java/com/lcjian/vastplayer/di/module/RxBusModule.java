package com.lcjian.vastplayer.di.module;

import androidx.annotation.NonNull;

import com.lcjian.vastplayer.RxBus;
import com.lcjian.vastplayer.di.scope.ApplicationScope;

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

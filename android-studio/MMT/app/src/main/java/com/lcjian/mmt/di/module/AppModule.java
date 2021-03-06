package com.lcjian.mmt.di.module;

import android.app.Application;

import com.lcjian.mmt.di.scope.ApplicationScope;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @NonNull
    private final Application application;

    public AppModule(@NonNull Application application) {
        this.application = application;
    }

    @Provides
    @NonNull
    @ApplicationScope
    public Application provideApplication() {
        return application;
    }
}

package com.lcjian.drinkwater.di.component;

import android.app.Application;
import android.content.SharedPreferences;

import com.lcjian.drinkwater.RxBus;
import com.lcjian.drinkwater.android.service.NotifyService;
import com.lcjian.drinkwater.data.db.AppDatabase;
import com.lcjian.drinkwater.data.network.RestAPI;
import com.lcjian.drinkwater.di.module.AppModule;
import com.lcjian.drinkwater.di.module.DbModule;
import com.lcjian.drinkwater.di.module.RestAPIModule;
import com.lcjian.drinkwater.di.module.RxBusModule;
import com.lcjian.drinkwater.di.module.SharedPreferenceModule;
import com.lcjian.drinkwater.di.scope.ApplicationScope;
import com.lcjian.drinkwater.ui.base.BaseActivity;
import com.lcjian.drinkwater.ui.base.BaseBottomSheetDialogFragment;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;
import com.lcjian.drinkwater.ui.base.BaseFragment;

import javax.inject.Named;

import dagger.Component;

@ApplicationScope
@Component(modules = {AppModule.class, DbModule.class, RestAPIModule.class, RxBusModule.class, SharedPreferenceModule.class})
public interface AppComponent {

    // 暴露给其他Component start
    Application application();

    AppDatabase appDatabase();

    RestAPI restAPI();

    RxBus rxBus();

    @Named("user_info")
    SharedPreferences userInfoSharedPreferences();

    @Named("setting")
    SharedPreferences settingSharedPreferences();
    // 暴露给其他Component end

    void inject(BaseActivity baseActivity);

    void inject(BaseFragment baseFragment);

    void inject(BaseDialogFragment baseDialogFragment);

    void inject(BaseBottomSheetDialogFragment baseBottomSheetDialogFragment);

    void inject(NotifyService notifyService);

}

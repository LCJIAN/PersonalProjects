package com.lcjian.cloudlocation.di.component;

import android.app.Application;
import android.content.SharedPreferences;

import com.lcjian.cloudlocation.RxBus;
import com.lcjian.cloudlocation.data.db.AppDatabase;
import com.lcjian.cloudlocation.data.network.RestAPI;
import com.lcjian.cloudlocation.di.module.AppModule;
import com.lcjian.cloudlocation.di.module.DbModule;
import com.lcjian.cloudlocation.di.module.RestAPIModule;
import com.lcjian.cloudlocation.di.module.RxBusModule;
import com.lcjian.cloudlocation.di.module.SharedPreferenceModule;
import com.lcjian.cloudlocation.di.scope.ApplicationScope;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.base.BaseBottomSheetDialogFragment;
import com.lcjian.cloudlocation.ui.base.BaseDialogFragment;
import com.lcjian.cloudlocation.ui.base.BaseFragment;

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

}

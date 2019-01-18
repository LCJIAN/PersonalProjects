package com.lcjian.osc.di.component;

import android.app.Application;
import android.content.SharedPreferences;

import com.lcjian.osc.RxBus;
import com.lcjian.osc.data.db.AppDatabase;
import com.lcjian.osc.data.network.RestAPI;
import com.lcjian.osc.di.module.AppModule;
import com.lcjian.osc.di.module.DbModule;
import com.lcjian.osc.di.module.RestAPIModule;
import com.lcjian.osc.di.module.RxBusModule;
import com.lcjian.osc.di.module.SharedPreferenceModule;
import com.lcjian.osc.di.scope.ApplicationScope;
import com.lcjian.osc.ui.base.BaseActivity;
import com.lcjian.osc.ui.base.BaseBottomSheetDialogFragment;
import com.lcjian.osc.ui.base.BaseDialogFragment;
import com.lcjian.osc.ui.base.BaseFragment;

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

package com.lcjian.mmt.di.component;

import android.app.Application;
import android.content.SharedPreferences;

import com.lcjian.mmt.RxBus;
import com.lcjian.mmt.data.db.AppDatabase;
import com.lcjian.mmt.data.network.RestAPI;
import com.lcjian.mmt.di.module.AppModule;
import com.lcjian.mmt.di.module.DbModule;
import com.lcjian.mmt.di.module.RestAPIModule;
import com.lcjian.mmt.di.module.RxBusModule;
import com.lcjian.mmt.di.module.SharedPreferenceModule;
import com.lcjian.mmt.di.scope.ApplicationScope;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.BaseBottomSheetDialogFragment;
import com.lcjian.mmt.ui.base.BaseDialogFragment;
import com.lcjian.mmt.ui.base.BaseFragment;

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

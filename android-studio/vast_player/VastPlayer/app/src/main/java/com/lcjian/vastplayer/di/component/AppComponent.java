package com.lcjian.vastplayer.di.component;

import android.app.Application;
import android.content.SharedPreferences;

import com.lcjian.vastplayer.RxBus;
import com.lcjian.vastplayer.android.service.MediaScannerService;
import com.lcjian.vastplayer.data.db.AppDatabase;
import com.lcjian.vastplayer.data.network.RestAPI;
import com.lcjian.vastplayer.di.module.AppModule;
import com.lcjian.vastplayer.di.module.DbModule;
import com.lcjian.vastplayer.di.module.RestAPIModule;
import com.lcjian.vastplayer.di.module.RxBusModule;
import com.lcjian.vastplayer.di.module.SharedPreferenceModule;
import com.lcjian.vastplayer.di.scope.ApplicationScope;
import com.lcjian.vastplayer.ui.base.BaseActivity;
import com.lcjian.vastplayer.ui.base.BaseBottomSheetDialogFragment;
import com.lcjian.vastplayer.ui.base.BaseDialogFragment;
import com.lcjian.vastplayer.ui.base.BaseFragment;

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

    void inject(MediaScannerService mediaScannerService);

}

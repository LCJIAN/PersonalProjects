package com.lcjian.osc.ui.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.lcjian.osc.App;
import com.lcjian.osc.RxBus;
import com.lcjian.osc.data.db.AppDatabase;
import com.lcjian.osc.data.network.RestAPI;
import com.lcjian.osc.di.component.AppComponent;

import javax.inject.Inject;
import javax.inject.Named;

public class BaseDialogFragment extends AppCompatDialogFragment {

    @Inject
    protected AppDatabase mAppDatabase;
    @Inject
    protected RestAPI mRestAPI;
    @Inject
    protected RxBus mRxBus;
    @Inject
    @Named("user_info")
    protected SharedPreferences mUserInfoSp;
    @Inject
    @Named("setting")
    protected SharedPreferences mSettingSp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateComponent(App.getInstance().appComponent());
    }

    protected void onCreateComponent(AppComponent appComponent) {
        appComponent.inject(this);
    }
}

package com.lcjian.drinkwater.ui.base;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.lcjian.drinkwater.App;
import com.lcjian.drinkwater.RxBus;
import com.lcjian.drinkwater.data.db.AppDatabase;
import com.lcjian.drinkwater.data.network.RestAPI;
import com.lcjian.drinkwater.di.component.AppComponent;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

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

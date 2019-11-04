package com.lcjian.cloudlocation.ui.base;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.RxBus;
import com.lcjian.cloudlocation.data.db.AppDatabase;
import com.lcjian.cloudlocation.data.network.RestAPI;
import com.lcjian.cloudlocation.di.component.AppComponent;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.annotation.Nullable;

public class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

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

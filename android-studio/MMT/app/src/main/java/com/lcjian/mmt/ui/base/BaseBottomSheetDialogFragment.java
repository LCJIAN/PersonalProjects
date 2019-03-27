package com.lcjian.mmt.ui.base;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lcjian.mmt.App;
import com.lcjian.mmt.RxBus;
import com.lcjian.mmt.data.db.AppDatabase;
import com.lcjian.mmt.data.network.RestAPI;
import com.lcjian.mmt.di.component.AppComponent;

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

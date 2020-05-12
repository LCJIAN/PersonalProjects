package com.lcjian.vastplayer.ui.base;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lcjian.vastplayer.App;
import com.lcjian.vastplayer.RxBus;
import com.lcjian.vastplayer.data.db.AppDatabase;
import com.lcjian.vastplayer.data.network.RestAPI;
import com.lcjian.vastplayer.di.component.AppComponent;

import javax.inject.Inject;
import javax.inject.Named;

public class BaseFragment extends Fragment {

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

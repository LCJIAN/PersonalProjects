package com.org.firefighting.ui.user;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.util.Pair;

import com.org.firefighting.R;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.main.MainActivity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SplashActivity extends BaseActivity {

    private Disposable mDisposable;
    private Disposable mDisposableP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_splash);

        checkPermission();
    }

    @Override
    protected void onDestroy() {
        mDisposableP.dispose();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    private void checkPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        mDisposableP = rxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.READ_PHONE_STATE)
                .subscribe(granted -> {
                    if (granted) {
                        navigateTo();
                    } else {
                        finish();
                    }
                });
    }

    private void navigateTo() {
        mDisposable = Single.zip(
                Single.fromCallable(SharedPreferencesDataSource::getGuided),
                Single.fromCallable(() -> SharedPreferencesDataSource.getSignInResponse() != null),
                (Pair::create))
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                            assert pair.first != null;
                            assert pair.second != null;
                            if (pair.second) {
                                startActivity(new Intent(this, MainActivity.class));
                            } else {
                                if (pair.first) {
                                    startActivity(new Intent(this, SignInActivity.class));
                                } else {
                                    SharedPreferencesDataSource.putGuided(true);
                                    startActivity(new Intent(this, GuideActivity.class));
                                }
                            }
                            finish();
                        },
                        throwable -> {
                        });
    }
}
package com.lcjian.vastplayer.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.github.glomadrian.grav.GravView;
import com.lcjian.lib.util.common.PackageUtils2;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.base.BaseActivity;
import com.lcjian.vastplayer.ui.home.MainActivity;
import com.lcjian.vastplayer.ui.mine.VideoLibActivity;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class SplashActivity extends BaseActivity {

    @BindView(R.id.fl_splash_ad_container)
    FrameLayout fl_splash_ad_container;
    @BindView(R.id.gr_view)
    GravView gr_view;

    private Subject<Boolean> mSubject;
    private CompositeDisposable mDisposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        mDisposables = new CompositeDisposable();
        mSubject = PublishSubject.create();

        RxPermissions rxPermissions = new RxPermissions(this);
        mDisposables.add(rxPermissions.request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        setup();
                    } else {
                        finish();
                    }
                }));
    }

    private void setup() {
        Observable<Map<String, String>> o = mRestAPI.spunSugarService().configs().cache();
        mDisposables.add(mSubject
                .flatMap(aBoolean -> o)
                .map(map -> Boolean.valueOf(map.get("has_content"))
                        && !TextUtils.equals(PackageUtils2.getVersionName(SplashActivity.this), map.get("verifying_version")))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hasContent -> {
                    if (hasContent) {
                        navigateToMain();
                    } else {
                        navigateToVideoLibrary();
                    }
                }, throwable -> navigateToVideoLibrary()));

        mDisposables.add(o
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringStringMap -> {
                    for (String key : stringStringMap.keySet()) {
                        mUserInfoSp.edit().putString(key, stringStringMap.get(key)).apply();
                    }
                }, throwable -> {

                }));

        new SplashAD(this, fl_splash_ad_container,
                Constants.QQ_ID, Constants.GTD_SPLASH_ID, new SplashADListener() {
            @Override
            public void onADDismissed() {
                fl_splash_ad_container.setVisibility(View.GONE);
                mSubject.onNext(true);
            }

            @Override
            public void onNoAD(AdError adError) {
                mSubject.onNext(true);
            }

            @Override
            public void onADPresent() {

            }

            @Override
            public void onADClicked() {
                mSubject.onNext(true);
            }

            @Override
            public void onADTick(long l) {

            }

            @Override
            public void onADExposure() {

            }
        });
    }

    @Override
    public void onPause() {
        try {
            if (gr_view != null) {
                gr_view.stop();
            }
        } catch (Exception ignore) {
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mDisposables.dispose();
        super.onDestroy();
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        finish();
    }

    private void navigateToVideoLibrary() {
        startActivity(new Intent(this, VideoLibActivity.class));
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        finish();
    }
}

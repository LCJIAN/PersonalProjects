package com.lcjian.drinkwater.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.lcjian.drinkwater.data.db.entity.DefaultConfig;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.ui.base.BaseActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SplashActivity extends BaseActivity {

    private Disposable mDisposable;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
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

        mDisposable = Single.just(true)
                .map(aBoolean -> {
                    List<Unit> units = mAppDatabase.unitDao().getAllSync();
                    if (units.isEmpty()) {
                        Unit unit1 = new Unit();
                        unit1.name = "kg,ml";
                        unit1.rate = 1d;
                        Unit unit2 = new Unit();
                        unit2.name = "lbs,fl oz";
                        unit2.rate = 2d;
                        mAppDatabase.unitDao().insert(unit1, unit2);
                    }
                    List<DefaultConfig> configs = mAppDatabase.defaultConfigDao().getAllSync();
                    if (configs.isEmpty()) {
                        DefaultConfig defaultConfig = new DefaultConfig();
                        defaultConfig.defaultGender = 0;
                        defaultConfig.defaultMaxWeight = 440d;
                        defaultConfig.defaultWeight = 70d;
                        defaultConfig.defaultUnitId = mAppDatabase.unitDao().getAllSyncByName("kg,ml").get(0).id;
                        defaultConfig.defaultWakeUpTime = "08:00";
                        defaultConfig.defaultSleepTime = "22:00";
                        mAppDatabase.defaultConfigDao().insert(defaultConfig);
                    }
                    return aBoolean;
                })
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                            startActivity(new Intent(this, GuideActivity.class),
                                    ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                getWindow().setExitTransition(new Fade().addTarget(getWindow().getDecorView()));
                                finishAfterTransition();
                            } else {
                                finish();
                            }
                        },
                        throwable -> {
                        });
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }
}

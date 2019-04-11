package com.lcjian.drinkwater.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Fade;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.lcjian.drinkwater.data.db.entity.Config;
import com.lcjian.drinkwater.data.db.entity.Cup;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.ui.base.BaseActivity;
import com.lcjian.drinkwater.util.ComputeUtils;
import com.lcjian.drinkwater.util.DateUtils;

import java.util.Arrays;
import java.util.Date;
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
                    List<Cup> cups = mAppDatabase.cupDao().getAllSync();
                    if (cups.isEmpty()) {
                        double[] capacity = new double[]{100, 200, 300, 400, 500};
                        Cup[] cupArr = new Cup[capacity.length];
                        for (int i = 0; i < capacity.length; i++) {
                            Cup cup = new Cup();
                            Date now = DateUtils.now();
                            cup.cupCapacity = capacity[i];
                            cup.timeAdded = now;
                            cup.timeModified = now;
                            cupArr[i] = cup;
                        }
                        mAppDatabase.cupDao().insert(cupArr);
                    }
                    List<Config> configs = mAppDatabase.configDao().getAllSync();
                    if (configs.isEmpty()) {
                        Config config = new Config();
                        config.minWeight = 1d;
                        config.maxWeight = 400d;
                        config.reminderIntervals = TextUtils.join(",", Arrays.asList("30", "45", "60", "90"));
                        mAppDatabase.configDao().insert(config);
                    }

                    List<Setting> settings = mAppDatabase.settingDao().getAllSync();
                    if (settings.isEmpty()) {
                        Setting setting = new Setting();

                        setting.unitId = mAppDatabase.unitDao().getAllSyncByName("kg,ml").get(0).id;
                        setting.gender = 0;
                        setting.weight = 70d;
                        setting.intakeGoal = ComputeUtils.computeDailyRecommendIntakeGoal(setting.weight, setting.gender);
                        setting.wakeUpTime = "08:00";
                        setting.sleepTime = "22:00";

                        setting.reminderInterval = 60;
                        setting.reminderMode = 2;
                        setting.reminderAlert = true;
                        setting.furtherReminder = true;

                        setting.cupId = mAppDatabase.cupDao().getAllSyncByCapacity(200).get(0).id;

                        mAppDatabase.settingDao().insert(setting);
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

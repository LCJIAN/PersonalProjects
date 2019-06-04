package com.lcjian.drinkwater.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.navigation.NavigationView;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.android.NotifyWorker;
import com.lcjian.drinkwater.data.db.entity.Record;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.ui.DrinkReportActivity;
import com.lcjian.drinkwater.ui.base.BaseActivity;
import com.lcjian.drinkwater.ui.setting.SettingActivity;
import com.lcjian.drinkwater.util.DateUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import q.rorbin.badgeview.QBadgeView;
import timber.log.Timber;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (getSupportFragmentManager().findFragmentByTag("MainFragment") == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_fragment_container, new MainFragment(), "MainFragment").commit();
        }

        shouldDrink(getIntent());
        setupWorker();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        shouldDrink(intent);
        mRxBus.send(new OEvent());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_remove_ads) {
            mSettingSp.edit().putBoolean("ad_clicked", true).apply();
        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(this, SettingActivity.class));
        } else if (id == R.id.nav_history) {
            startActivity(new Intent(this, DrinkReportActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupAdBadge();
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        mDisposable2.dispose();
        super.onDestroy();
    }

    private void shouldDrink(Intent intent) {
        if (intent.getBooleanExtra("drunk_water", false)) {

            NotificationManagerCompat.from(this).cancel(1000);

            navigationView.postDelayed(() -> {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag("MainFragment");
                if (fragment != null) {
                    ((MainFragment) fragment).performDrink();
                }
            }, 100);
        }
    }

    private void setupAdBadge() {
        if (!mSettingSp.getBoolean("ad_clicked", false)) {
            navigationView.post(() -> new QBadgeView(this)
                    .bindTarget(((ViewGroup) ((ViewGroup) navigationView
                            .getChildAt(0))
                            .getChildAt(1))
                            .getChildAt(0))
                    .setBadgeText("NEW"));
        }
    }

    private Disposable mDisposable;
    private Disposable mDisposable2;

    private long mNextNotifyTimeLg;  // 下次提醒时间 时间戳
    private String mNextNotifyTime;  // 下次提醒时间 HH:mm

    private void setupWorker() {
        mDisposable = Flowable.combineLatest(
                mRxBus.asFlowable().filter(o -> o instanceof OEvent),
                mAppDatabase.settingDao().getAllAsync().map(settings -> settings.get(0)),
                mAppDatabase.recordDao().getLatestAsync().map(records -> records.isEmpty() ? new Record() : records.get(0)),
                (o, setting, record) -> new DataHolder(setting, record))
                .subscribeOn(Schedulers.io())
                .subscribe(dataHolder -> {

                            Date today = DateUtils.today();
                            Date now = DateUtils.convertStrToDate(DateUtils.convertDateToStr(DateUtils.now(), "yyyy-MM-dd HH:mm"), "yyyy-MM-dd HH:mm"); // 精确到分
                            String nowStr = DateUtils.convertDateToStr(now);
                            Date wakeUpTime = DateUtils.convertStrToDate(nowStr + " " + dataHolder.setting.wakeUpTime, "yyyy-MM-dd HH:mm"); // 精确到分
                            Date sleepTime = DateUtils.convertStrToDate(nowStr + " " + dataHolder.setting.sleepTime, "yyyy-MM-dd HH:mm"); // 精确到分

                            boolean shouldNotify = dataHolder.setting.reminderMode != 0; // mute & auto
                            boolean intakeGoalReached = false;   // 是否到达今日饮水目标

                            // 判断是否到达今日饮水目标
                            {
                                List<Record> records = mAppDatabase.recordDao().getAllSyncByTime(today, DateUtils.addDays(today, 1)); // 获取今日饮水记录
                                double total = 0d;
                                for (Record record : records) {
                                    total += record.intake;
                                }
                                if (total >= dataHolder.setting.intakeGoal) {            // 已到达每日喝水量
                                    intakeGoalReached = true;
                                }
                            }

                            long delayTime = 0;

                            if (shouldNotify) {

                                boolean isSleeping; // 当前是否处于睡觉时间内
                                if (DateUtils.isBefore(wakeUpTime, sleepTime)) { // 起床时间在一天中早于睡觉时间
                                    isSleeping = DateUtils.isBefore(now, wakeUpTime) || DateUtils.isAfter(now, sleepTime);
                                    if (isSleeping) {
                                        if (DateUtils.isBefore(now, wakeUpTime)) {
                                            delayTime = wakeUpTime.getTime() + 10 * 60 * 1000 - now.getTime(); // 起床10分钟后
                                        } else {
                                            delayTime = DateUtils.addDays(wakeUpTime, 1).getTime() + 10 * 60 * 1000 - now.getTime(); // 第二天起床10分钟后
                                        }
                                    }
                                } else {                                         // 起床时间在一天中晚于睡觉时间
                                    isSleeping = DateUtils.isBefore(now, wakeUpTime) && DateUtils.isAfter(now, sleepTime);
                                    if (isSleeping) {
                                        delayTime = wakeUpTime.getTime() + 10 * 60 * 1000 - now.getTime(); // 起床10分钟后
                                    }
                                }

                                if (!isSleeping) {
                                    if (intakeGoalReached) {                              // 到达饮水目标
                                        if (dataHolder.setting.furtherReminder) {         // 持续提醒
                                            delayTime = dataHolder.latestRecord.timeAdded.getTime() + dataHolder.setting.reminderInterval * 60 * 1000 - now.getTime();
                                        } else {                                          // 持续提醒关闭
                                            delayTime = DateUtils.addDays(wakeUpTime, 1).getTime() + 10 * 60 * 1000 - now.getTime(); // 第二天起床10分钟后
                                        }
                                    } else {                                              // 未到达饮水目标
                                        if (dataHolder.latestRecord.timeAdded == null) {  // 未喝过水
                                            if (TextUtils.isEmpty(mNextNotifyTime)) {
                                                delayTime = 60 * 1000;                    // 1分钟后
                                            } else {
                                                delayTime = -1;                           // 延迟
                                            }
                                        } else {
                                            delayTime = dataHolder.latestRecord.timeAdded.getTime() + dataHolder.setting.reminderInterval * 60 * 1000 - now.getTime();
                                        }
                                    }
                                }

                                if (delayTime < 0) {                                      // 时间超过了未喝水
                                    long lastTime;
                                    long interval;
                                    if (dataHolder.setting.reminderAlert) {               // 错过Intake time继续remind在上一次提醒20分钟后
                                        interval = 20 * 60 * 1000;
                                        lastTime = mNextNotifyTimeLg;
                                    } else {                                              // 上一次喝水在reminderInterval分钟后
                                        interval = dataHolder.setting.reminderInterval * 60 * 1000;
                                        lastTime = dataHolder.latestRecord.timeAdded == null ? mNextNotifyTimeLg : dataHolder.latestRecord.timeAdded.getTime();
                                    }
                                    long duration = now.getTime() - lastTime;
                                    if (duration < 0) {
                                        delayTime = lastTime - now.getTime();
                                    } else {
                                        if (duration == 0) {
                                            delayTime = interval;
                                        } else {
                                            long count = duration % interval == 0 ? duration / interval : duration / interval + 1;
                                            delayTime = lastTime + count * interval - now.getTime();
                                        }
                                    }
                                }

                                WorkManager.getInstance().enqueueUniqueWork(
                                        "NotifyWorker",
                                        ExistingWorkPolicy.REPLACE,
                                        new OneTimeWorkRequest
                                                .Builder(NotifyWorker.class)
                                                .setInitialDelay(delayTime, TimeUnit.MILLISECONDS)
                                                .build());

                                // 通知首页更新UI
                                mNextNotifyTimeLg = now.getTime() + delayTime;
                                mNextNotifyTime = DateUtils.convertDateToStr(new Date(mNextNotifyTimeLg), "HH:mm");
                                navigationView.post(() -> {
                                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("MainFragment");
                                    if (fragment != null) {
                                        ((MainFragment) fragment).setNextNotifyTime(mNextNotifyTime);
                                    }
                                });
                            }
                        },
                        Timber::e);

        mDisposable2 = Single.just(true)
                .delay(2, TimeUnit.SECONDS)
                .subscribe(aBoolean -> mRxBus.send(new OEvent()));
    }

    private static class DataHolder {
        private Setting setting;
        private Record latestRecord;

        private DataHolder(Setting setting, Record latestRecord) {
            this.setting = setting;
            this.latestRecord = latestRecord;
        }
    }

    private static class OEvent {

    }

}

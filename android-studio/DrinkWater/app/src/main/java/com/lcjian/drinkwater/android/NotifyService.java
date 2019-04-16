package com.lcjian.drinkwater.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.View;

import com.lcjian.drinkwater.App;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.AppDatabase;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.di.component.AppComponent;
import com.lcjian.drinkwater.ui.home.MainActivity;
import com.lcjian.lib.window.Floating;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NotifyService extends Service {


    @Inject
    protected AppDatabase mAppDatabase;

    private NotifyReceiver mNotifyReceiver;
    private ScreenReceiver mScreenReceiver;

    private Disposable mDisposable;


    private boolean mScreenOn;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        onCreateComponent(App.getInstance().appComponent());

        {
            mNotifyReceiver = new NotifyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(NotifyReceiver.ACTION_NOTIFY);
            registerReceiver(mNotifyReceiver, intentFilter);
        }
        {
            mScreenReceiver = new ScreenReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(mScreenReceiver, intentFilter);
        }

        setupWorker();
    }

    private void onCreateComponent(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mNotifyReceiver);
        unregisterReceiver(mScreenReceiver);
        mDisposable.dispose();
        super.onDestroy();
    }

    private void setupWorker() {
        mDisposable = mAppDatabase.settingDao().getAllAsync().map(settings -> settings.get(0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(setting -> {
                    if (setting.reminderMode == 0) {     // off
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        alarmManager.cancel(PendingIntent.getBroadcast(this,
                                0, new Intent(this, NotifyReceiver.class), 0));
                    } else {
                        if (setting.reminderMode == 1) { // mute

                        } else {                         // auto

                        }

                        if (setting.reminderAlert) {     // 解锁提醒

                        } else {

                        }

                        if (setting.furtherReminder) {   // 持续提醒

                        } else {

                        }
                    }

//                    if (mLastSetting == null || mLastSetting.reminderAlert != setting.reminderAlert) {
//                        if (setting.reminderAlert) {
//                            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest
//                                    .Builder(NotifyWorker.class, setting.reminderInterval, TimeUnit.MINUTES)
//                                    .build();
//                            WorkManager.getInstance().enqueueUniquePeriodicWork(
//                                    "NotifyWorker",
//                                    ExistingPeriodicWorkPolicy.REPLACE,
//                                    periodicWorkRequest);
//                        } else {
//                            WorkManager.getInstance().cancelUniqueWork("NotifyWorker");
//                        }
//                        mLastSetting = setting;
//                    }
                }, throwable -> {
                });
    }


    public class NotifyReceiver extends BroadcastReceiver {

        public static final String ACTION_NOTIFY = "drink.water.ACTION_NOTIFY";

        @Override
        public void onReceive(Context context, Intent intent) {
            Floating floating = new Floating(context);
            View view = floating.setContentView(R.layout.floating_pop_view);
            view.findViewById(R.id.btn_drink).setOnClickListener(v -> {
                startActivity(new Intent(v.getContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                floating.dismiss();
            });

            floating.setViewDismissHandler(() -> {

            });
            floating.show();
        }
    }

    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

        }
    }
}

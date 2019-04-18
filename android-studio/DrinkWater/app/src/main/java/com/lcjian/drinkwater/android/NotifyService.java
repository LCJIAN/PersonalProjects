package com.lcjian.drinkwater.android;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;
import android.view.View;

import com.lcjian.drinkwater.App;
import com.lcjian.drinkwater.BuildConfig;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.RxBus;
import com.lcjian.drinkwater.data.db.AppDatabase;
import com.lcjian.drinkwater.data.db.entity.Record;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.di.component.AppComponent;
import com.lcjian.drinkwater.ui.home.MainActivity;
import com.lcjian.drinkwater.ui.home.MainFragment;
import com.lcjian.drinkwater.util.DateUtils;
import com.lcjian.drinkwater.util.Utils;
import com.lcjian.lib.window.Floating;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NotifyService extends Service {

    @Inject
    protected AppDatabase mAppDatabase;

    @Inject
    protected RxBus mRxBus;

    private NotifyReceiver mNotifyReceiver;
    private ScreenOnOffReceiver mScreenOnOffReceiver;

    private Disposable mDisposable;
    private Disposable mDisposable2;
    private Disposable mDisposable3;

    private SoundPool mSoundPool;
    private int mSoundID;

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
            mScreenOnOffReceiver = new ScreenOnOffReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            registerReceiver(mScreenOnOffReceiver, intentFilter);
        }
        mSoundPool = new SoundPool(1,// 同时播放的音效
                AudioManager.STREAM_MUSIC, 0);
        mSoundID = mSoundPool.load(this, R.raw.water, 1);
        setupWorker();
    }

    private void onCreateComponent(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mNotifyReceiver);
        unregisterReceiver(mScreenOnOffReceiver);
        mDisposable.dispose();
        if (mDisposable2 != null) {
            mDisposable2.dispose();
        }
        if (mDisposable3 != null) {
            mDisposable3.dispose();
        }
        mSoundPool.release();
        super.onDestroy();
    }

    private void setupWorker() {
        mDisposable = Flowable.combineLatest(
                mRxBus.asFlowable().filter(o -> o instanceof FloatingShowDismissEvent).map(o -> ((FloatingShowDismissEvent) o).isShowing),
                mRxBus.asFlowable().filter(o -> o instanceof ScreenOnOffEvent).map(o -> ((ScreenOnOffEvent) o).isScreenOn),
                mAppDatabase.settingDao().getAllAsync().map(settings -> settings.get(0)),
                mAppDatabase.recordDao().getLatestAsync().map(records -> records.isEmpty() ? new Record() : records.get(0)),
                DataHolder::new)
                .subscribeOn(Schedulers.io())
                .subscribe(dataHolder -> {
                            if (mDisposable2 != null) {
                                mDisposable2.dispose();
                            }
                            if (dataHolder.isShowing) {
                                return;
                            }
                            boolean shouldNotify = false;
                            if (dataHolder.setting.reminderMode != 0) {     // mute & auto
                                if (dataHolder.setting.reminderAlert) {     // 解锁提醒
                                    if (dataHolder.isScreenOn) {
                                        shouldNotify = true;
                                    }
                                } else {
                                    shouldNotify = true;
                                }

                                if (shouldNotify) {
                                    if (!dataHolder.setting.furtherReminder) {   // 持续提醒关闭
                                        Date today = DateUtils.today();
                                        List<Record> records = mAppDatabase.recordDao().getAllSyncByTime(today, DateUtils.addDays(today, 1));
                                        double total = 0d;
                                        for (Record record : records) {
                                            total += record.intake;
                                        }
                                        if (total >= dataHolder.setting.intakeGoal) {
                                            shouldNotify = false;
                                        }
                                    }
                                }

                                if (shouldNotify) { // 判断时间是否在睡觉之后
                                    Date now = DateUtils.now();
                                    String nowStr = DateUtils.convertDateToStr(now);
                                    Date wakeUpTime = DateUtils.convertStrToDate(nowStr + " " + dataHolder.setting.wakeUpTime, "yyyy-MM-dd HH:mm");
                                    Date sleepTime = DateUtils.convertStrToDate(nowStr + " " + dataHolder.setting.sleepTime, "yyyy-MM-dd HH:mm");
                                    if (DateUtils.isAfter(wakeUpTime, sleepTime)) {
                                        sleepTime = DateUtils.addDays(sleepTime, 1);
                                    }
                                    if (DateUtils.isAfter(now, sleepTime)) {
                                        shouldNotify = false;
                                    }
                                }
                            }

                            if (shouldNotify) {
                                long delayTime;
                                if (dataHolder.record.timeAdded == null) {
                                    delayTime = 60 * 1000;
                                } else {
                                    delayTime = dataHolder.record.timeAdded.getTime() + dataHolder.setting.reminderInterval * 60 * 1000 - DateUtils.now().getTime();
                                    if (delayTime < 0) {
                                        delayTime = 20 * 60 * 1000;
                                    }
                                }

                                mDisposable2 = Flowable.just(true)
                                        .delay(delayTime, TimeUnit.MILLISECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                aBoolean -> sendBroadcast(new Intent().setAction(NotifyReceiver.ACTION_NOTIFY)),
                                                throwable -> {
                                                });
                                sendBroadcast(new Intent()
                                        .putExtra("next_notify_time", DateUtils.convertDateToStr(new Date(System.currentTimeMillis() + delayTime), "HH:mm"))
                                        .setAction(MainFragment.NextNotifyTimeReceiver.ACTION_NEXT_NOTIFY_TIME));
                            }
                        },
                        throwable -> {
                        });

        mDisposable3 = Flowable.just(true)
                .delay(2, TimeUnit.SECONDS)
                .subscribe(
                        aBoolean -> {
                            mRxBus.send(new FloatingShowDismissEvent(false));
                            mRxBus.send(new ScreenOnOffEvent(true));
                        },
                        throwable -> {
                        });
    }

    private static class DataHolder {
        private Boolean isShowing;
        private Boolean isScreenOn;
        private Setting setting;
        private Record record;

        private DataHolder(Boolean isShowing, Boolean isScreenOn, Setting setting, Record record) {
            this.isShowing = isShowing;
            this.isScreenOn = isScreenOn;
            this.setting = setting;
            this.record = record;
        }
    }

    private static class ScreenOnOffEvent {

        private Boolean isScreenOn;

        private ScreenOnOffEvent(Boolean isScreenOn) {
            this.isScreenOn = isScreenOn;
        }
    }

    private static class FloatingShowDismissEvent {

        private Boolean isShowing;

        private FloatingShowDismissEvent(Boolean isShowing) {
            this.isShowing = isShowing;
        }
    }

    public class NotifyReceiver extends BroadcastReceiver {

        public static final String ACTION_NOTIFY = "drink.water.ACTION_NOTIFY";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utils.isBackground(context)) {
                Notification notification = new NotificationCompat.Builder(context, BuildConfig.APPLICATION_ID)
                        .setContentTitle(context.getString(R.string.hg))
                        .setContentText(context.getString(R.string.iiii))
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setSmallIcon(android.R.drawable.stat_notify_more)
                        .setAutoCancel(true)
                        .build();
                NotificationManagerCompat.from(context)
                        .notify(1000, notification);
            }

            mSoundPool.play(mSoundID, 0.7f, 0.7f, 0, 0, 1);

            Floating floating = new Floating(context);
            View view = floating.setContentView(R.layout.floating_pop_view);
            view.findViewById(R.id.btn_drink).setOnClickListener(v -> {
                startActivity(new Intent(v.getContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("drunk_water", true));
                floating.dismiss();
            });

            floating.setViewDismissHandler(() -> mRxBus.send(new FloatingShowDismissEvent(false)));
            floating.show();
            mRxBus.send(new FloatingShowDismissEvent(true));
        }
    }

    public class ScreenOnOffReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                mRxBus.send(new ScreenOnOffEvent(false));
            }
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                mRxBus.send(new ScreenOnOffEvent(true));
            }
        }
    }
}

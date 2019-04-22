package com.lcjian.drinkwater.android.service;

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
import android.os.RemoteCallbackList;
import android.os.RemoteException;
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
import androidx.core.util.Pair;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class NotifyService extends Service {

    @Inject
    protected AppDatabase mAppDatabase;

    @Inject
    protected RxBus mRxBus;

    private NotifyReceiver mNotifyReceiver;
    private ScreenOnOffReceiver mScreenOnOffReceiver;
    private SettingRecordChangeReceiver mSettingRecordChangeReceiver;

    private Disposable mDisposable;
    private Disposable mDisposable2;
    private Disposable mDisposable3;

    private SoundPool mSoundPool;
    private int mSoundID;

    private String mNextNotifyTime;

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
        {
            mSettingRecordChangeReceiver = new SettingRecordChangeReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SettingRecordChangeReceiver.ACTION_SETTING_RECORD_CHANGE);
            registerReceiver(mSettingRecordChangeReceiver, intentFilter);
        }
        mSoundPool = new SoundPool(1,// 同时播放的音效
                AudioManager.STREAM_MUSIC, 0);
        mSoundID = mSoundPool.load(this, R.raw.water, 1);
        setupWorker();
    }

    private void onCreateComponent(AppComponent appComponent) {
        appComponent.inject(this);
    }

    private void setupWorker() {
        mDisposable = Flowable.combineLatest(
                mRxBus.asFlowable().filter(o -> o instanceof FloatingShowDismissEvent).map(o -> ((FloatingShowDismissEvent) o).isShowing),
                mRxBus.asFlowable().filter(o -> o instanceof ScreenOnOffEvent).map(o -> ((ScreenOnOffEvent) o).isScreenOn),
                mRxBus.asFlowable().filter(o -> o instanceof SettingRecordChangeEvent).map(o -> {
                    Setting setting = mAppDatabase.settingDao().getAllSync().get(0);
                    List<Record> records = mAppDatabase.recordDao().getLatestSync();
                    Record record = records.isEmpty() ? new Record() : records.get(0);
                    return Pair.create(setting, record);
                }),
                DataHolder::new)
                .subscribeOn(Schedulers.io())
                .subscribe(dataHolder -> {
                            if (mDisposable2 != null) {
                                mDisposable2.dispose();
                            }
                            if (dataHolder.isShowing) {
                                return;
                            }

                            Date today = DateUtils.today();
                            Date now = DateUtils.now();
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
                            }

                            if (shouldNotify) {

                                long delayTime = 0;

                                boolean isSleeping;
                                String nowStr = DateUtils.convertDateToStr(now);
                                Date wakeUpTime = DateUtils.convertStrToDate(nowStr + " " + dataHolder.setting.wakeUpTime, "yyyy-MM-dd HH:mm");
                                Date sleepTime = DateUtils.convertStrToDate(nowStr + " " + dataHolder.setting.sleepTime, "yyyy-MM-dd HH:mm");
                                if (DateUtils.isBefore(wakeUpTime, sleepTime)) {
                                    isSleeping = DateUtils.isBefore(now, wakeUpTime) || DateUtils.isAfter(now, sleepTime);
                                    if (isSleeping) {
                                        if (DateUtils.isBefore(now, wakeUpTime)) {
                                            delayTime = wakeUpTime.getTime() + 10 * 60 * 1000 - now.getTime(); // 起床10分钟后
                                        } else {
                                            delayTime = DateUtils.addDays(wakeUpTime, 1).getTime() + 10 * 60 * 1000 - now.getTime(); // 起床10分钟后
                                        }
                                    }
                                } else {
                                    isSleeping = DateUtils.isBefore(now, wakeUpTime) && DateUtils.isAfter(now, sleepTime);
                                    if (isSleeping) {
                                        delayTime = wakeUpTime.getTime() + 10 * 60 * 1000 - now.getTime(); // 起床10分钟后
                                    }
                                }

                                if (!isSleeping) {
                                    if (dataHolder.latestRecord.timeAdded == null) {  // 未喝过水
                                        delayTime = 60 * 1000;                  // 1分钟后
                                    } else {
                                        delayTime = dataHolder.latestRecord.timeAdded.getTime() + dataHolder.setting.reminderInterval * 60 * 1000 - now.getTime();
                                        if (delayTime < 0) {                    // 时间到了未喝水
                                            delayTime = 20 * 60 * 1000;         // 20分钟后
                                        }
                                    }
                                }

                                mDisposable2 = Flowable.just(true)
                                        .delay(delayTime, TimeUnit.MILLISECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                aBoolean -> sendBroadcast(new Intent().setAction(NotifyReceiver.ACTION_NOTIFY)),
                                                throwable -> {
                                                });
                                notifyNextNotifyTimeChanged(DateUtils.convertDateToStr(new Date(now.getTime() + delayTime), "HH:mm"));
                            }
                        },
                        Timber::e);

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

    @Override
    public void onDestroy() {
        unregisterReceiver(mNotifyReceiver);
        unregisterReceiver(mScreenOnOffReceiver);
        unregisterReceiver(mSettingRecordChangeReceiver);
        mDisposable.dispose();
        if (mDisposable2 != null) {
            mDisposable2.dispose();
        }
        if (mDisposable3 != null) {
            mDisposable3.dispose();
        }
        mSoundPool.release();
        mCallbacks.kill();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    final RemoteCallbackList<INotifierCallback> mCallbacks = new RemoteCallbackList<>();

    private final INotifier.Stub mBinder = new INotifier.Stub() {

        @Override
        public String getNextNotifyTime() {
            return mNextNotifyTime;
        }

        @Override
        public void registerCallback(INotifierCallback cb) {
            if (cb != null) mCallbacks.register(cb);
        }

        @Override
        public void unregisterCallback(INotifierCallback cb) {
            if (cb != null) mCallbacks.unregister(cb);
        }

    };

    private void notifyNextNotifyTimeChanged(String nextNotifyTime) {
        mNextNotifyTime = nextNotifyTime;
        // Broadcast to all clients the new value.
        final int N = mCallbacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).onNextNotifyTimeChanged(nextNotifyTime);
            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
                Timber.d(e);
            }
        }
        mCallbacks.finishBroadcast();
    }


    private static class DataHolder {
        private Boolean isShowing;
        private Boolean isScreenOn;
        private Setting setting;
        private Record latestRecord;

        private DataHolder(Boolean isShowing, Boolean isScreenOn, Pair<Setting, Record> pair) {
            this.isShowing = isShowing;
            this.isScreenOn = isScreenOn;
            this.setting = pair.first;
            this.latestRecord = pair.second;
        }
    }

    private static class SettingRecordChangeEvent {

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

    public class SettingRecordChangeReceiver extends BroadcastReceiver {

        public static final String ACTION_SETTING_RECORD_CHANGE = "drink.water.ACTION_SETTING_RECORD_CHANGE";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SETTING_RECORD_CHANGE.equals(intent.getAction())) {
                mRxBus.send(new SettingRecordChangeEvent());
            }
        }
    }
}

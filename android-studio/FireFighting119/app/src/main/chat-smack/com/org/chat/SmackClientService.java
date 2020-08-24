package com.org.chat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.lcjian.lib.util.common.FileUtils;
import com.lcjian.lib.util.common.ServiceUtils;
import com.org.chat.data.entity.JsonExtraBodyItem;
import com.org.firefighting.App;
import com.org.firefighting.BuildConfig;
import com.org.firefighting.R;
import com.org.firefighting.ui.user.SplashActivity;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;

import java.util.Collections;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.leolin.shortcutbadger.ShortcutBadger;
import timber.log.Timber;

public class SmackClientService extends Service {

    private Ringtone mRingtone;

    private SmackClient mSmackClient;

    private SmackClient.MessageListener mMessageListener = new SmackClient.MessageListener() {
        @Override
        public void newOutgoingMessage(Message message) {
            insertOutgoingMessage(message);
        }

        @Override
        public void newIncomingMessage(Message message) {
            insertIncomingMessage(message);
        }

        @Override
        public void newOfflineMessage(Message message) {
            insertIncomingMessage(message);
        }

        @Override
        public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
            Timber.d("onReceiptReceived:" + receiptId + " " + receipt.toString());
        }
    };

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onDataConnectionStateChanged(int state) {
            if (state == TelephonyManager.DATA_CONNECTED) {
                if (mSmackClient != null) { // 按道理不为空，但是在有些手机上取消监听不成功或者有延迟
                    mSmackClient.restartAsync();
                }
            }
        }
    };

    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = null;
            if (cm != null) {
                info = cm.getActiveNetworkInfo();
            }
            if (info != null && info.isAvailable()) {
                mSmackClient.restartAsync();
            }
        }
    };

    private LifecycleObserver mLifecycleObserver = new LifecycleObserver() {

        private Disposable mDisposable;

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onAppForeground() {
            if (mDisposable != null) {
                mDisposable.dispose();
                mDisposable = null;
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onAppBackground() {
            mDisposable = App.getInstance().getAppDatabase().messageDao()
                    .getTotalUnReadCountRX(mSmackClient.getUser().asEntityBareJidString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> ShortcutBadger.applyCount(getApplicationContext(), integer));
        }
    };

    private MessageReceiver mMessageReceiver = new MessageReceiver();

    private LocalBinder mLocalBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SmackClientService getService() {
            return SmackClientService.this;
        }
    }

    public SmackClient getSmackClient() {
        return mSmackClient;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate");
        super.onCreate();
        mSmackClient = new SmackClient();
        mSmackClient.addMessageListener(mMessageListener);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        }
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(mConnectivityReceiver, filter);
        }
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(MessageReceiver.ACTION_MESSAGE);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(mLifecycleObserver);

        mRingtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand");
        if (intent != null) {
            if (TextUtils.equals("start", intent.getStringExtra("action"))) {
                String username = intent.getStringExtra("username");
                String password = intent.getStringExtra("password");
                if (!TextUtils.isEmpty(username)) {
                    mSmackClient.startAsync(username, password);
                }
            } else {
                mSmackClient.stopAsync();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");
        mRingtone.stop();
        ProcessLifecycleOwner.get().getLifecycle().removeObserver(mLifecycleObserver);
        mRingtone = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        unregisterReceiver(mConnectivityReceiver);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        mSmackClient.removeMessageListener(mMessageListener);
        mSmackClient.stopAsync();
        mSmackClient.destroy();
        mSmackClient = null;
        super.onDestroy();
    }

    public static void start(Context context, String username, String password) {
        context.startService(new Intent(context, SmackClientService.class)
                .putExtra("action", "start")
                .putExtra("username", username)
                .putExtra("password", password));
    }

    public static void stop(Context context) {
        context.startService(new Intent(context, SmackClientService.class)
                .putExtra("action", "stop"));
    }

    private void insertIncomingMessage(Message message) {
        JsonExtraBodyItem item = new Gson().fromJson(message.getBody("json_extra_body_item_1"), JsonExtraBodyItem.class);
        if (item != null) {
            item = convert(item);
        }
        Jid from = message.getFrom();
        Jid to = message.getTo();

        com.org.chat.data.db.entity.Message msg = new com.org.chat.data.db.entity.Message();
        msg.stanzaId = message.getStanzaId();
        msg.fromEntityBareJid = from.asEntityBareJidOrThrow().toString();
        msg.fromResource = from.hasResource() ? from.getResourceOrThrow().toString() : null;
        msg.toEntityBareJid = to.asEntityBareJidOrThrow().toString();
        msg.toResource = to.hasResource() ? to.getResourceOrThrow().toString() : null;
        msg.messageType = message.getType().ordinal();
        msg.body = message.getBody();
        msg.otherBodies = item == null ? null : new Gson().toJson(Collections.singletonList(item));
        msg.deliveryStatus = com.org.chat.data.db.entity.Message.DS_RECEIVED;
        msg.localStatus = com.org.chat.data.db.entity.Message.LS_NEW;
        msg.createTime = new Date();
        if (TextUtils.isEmpty(msg.body) && TextUtils.isEmpty(msg.otherBodies)) {
            return;
        }
        App.getInstance().getAppDatabase().messageDao().insert(msg);

        String fromJid = msg.fromEntityBareJid;
        String title;
        String content;
        String iconPath;

        if (TextUtils.isEmpty(msg.body)) {
            if (msg.otherBodies.contains("image")) {
                content = "[图片]";
            } else {
                content = "[语音]";
            }
        } else {
            content = msg.body;
        }

        VCard vCard = VCardLoader.getInstance(mSmackClient).getVCard(from.asEntityBareJidOrThrow());
        if (vCard != null) {
            title = vCard.getNickName();
            iconPath = vCard.getField("avatar_local_path");
        } else {
            title = "";
            iconPath = "";
        }

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(this, MessageReceiver.class)
                        .putExtra("from_jid", fromJid)
                        .putExtra("title", title)
                        .putExtra("content", content)
                        .putExtra("icon_path", iconPath)
                        .setAction(MessageReceiver.ACTION_MESSAGE));
    }

    private void insertOutgoingMessage(Message message) {
        JsonExtraBodyItem item = new Gson().fromJson(message.getBody("json_extra_body_item_1"), JsonExtraBodyItem.class);
        if (item != null) {
            item = convert(item);
        }
        EntityFullJid from = mSmackClient.getUser();
        Jid to = message.getTo();

        com.org.chat.data.db.entity.Message msg = new com.org.chat.data.db.entity.Message();
        msg.stanzaId = message.getStanzaId();
        msg.fromEntityBareJid = from.asEntityBareJidString();
        msg.fromResource = from.hasResource() ? from.getResourceOrThrow().toString() : null;
        msg.toEntityBareJid = to.asEntityBareJidOrThrow().toString();
        msg.toResource = to.hasResource() ? to.getResourceOrThrow().toString() : null;
        msg.messageType = message.getType().ordinal();
        msg.body = message.getBody();
        msg.otherBodies = item == null ? null : new Gson().toJson(Collections.singletonList(item));
        msg.deliveryStatus = com.org.chat.data.db.entity.Message.DS_SENT;
        msg.localStatus = com.org.chat.data.db.entity.Message.LS_NEW;
        msg.createTime = new Date();
        if (TextUtils.isEmpty(msg.body) && TextUtils.isEmpty(msg.otherBodies)) {
            return;
        }
        App.getInstance().getAppDatabase().messageDao().insert(msg);
    }

    /**
     * Base64数据转换，避免直接存入数据库
     */
    private static JsonExtraBodyItem convert(JsonExtraBodyItem item) {
        String contentType = item.contentType;
        String[] array = contentType.split(";");
        if (TextUtils.equals(array[0], "image/jpeg")) {
            if (TextUtils.equals(array[1], "base64")) {
                String path = Constants.DIRECTORY_IMAGE.getPath();
                String name = System.currentTimeMillis() + ".jpg";
                FileUtils.base64ToFile(path, name, item.content);
                JsonExtraBodyItem result = new JsonExtraBodyItem();
                result.contentType = "image/jpeg;filepath";
                result.content = path + "/" + name;
                return result;
            }
        } else if (TextUtils.equals(array[0], "audio/amr")) {
            if (TextUtils.equals(array[1], "base64")) {
                String path = Constants.DIRECTORY_VOICE.getPath();
                String name = System.currentTimeMillis() + ".amr";
                FileUtils.base64ToFile(path, name, item.content);
                JsonExtraBodyItem result = new JsonExtraBodyItem();
                result.contentType = "audio/amr;filepath;" + array[2];
                result.content = path + "/" + name;
                return result;
            }
        }
        return item;
    }

    public class MessageReceiver extends BroadcastReceiver {

        public static final String ACTION_MESSAGE = "com.org.smack.ACTION_MESSAGE";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ServiceUtils.isBackground(context)) { // App页面不处于前台时显示通知
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // create android channel
                    NotificationChannel androidChannel = new NotificationChannel(BuildConfig.APPLICATION_ID,
                            "ANDROID CHANNEL", NotificationManager.IMPORTANCE_DEFAULT);
                    // Sets whether notifications posted to this channel should display notification lights
                    androidChannel.enableLights(true);
                    // Sets whether notification posted to this channel should vibrate.
                    androidChannel.enableVibration(true);
                    // Sets the notification light color for notifications posted to this channel
                    androidChannel.setLightColor(Color.GREEN);
                    // Sets whether notifications posted to this channel appear on the lockscreen or not
                    androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

                    NotificationManager notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(androidChannel);
                    }
                }

                Bitmap largeIcon = BitmapFactory.decodeFile(intent.getStringExtra("icon_path"));
                if (largeIcon == null) {
                    largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
                }
                Notification notification = new NotificationCompat.Builder(context, BuildConfig.APPLICATION_ID)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setSmallIcon(android.R.drawable.stat_notify_more)
                        .setContentTitle(intent.getStringExtra("title"))
                        .setContentText(intent.getStringExtra("content"))
                        .setLargeIcon(largeIcon)
                        /*.setSound(Uri.parse(("android.resource://" + context.getPackageName() + "/" + R.raw.water)))*/
                        .setContentIntent(PendingIntent.getActivity(
                                context,
                                1000,
                                new Intent(context, SplashActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                                PendingIntent.FLAG_ONE_SHOT))
                        .setAutoCancel(true)
                        .build();

                String jid = intent.getStringExtra("jid");
                int notificationId = TextUtils.isEmpty(jid) ? 1000 : jid.hashCode();
                NotificationManagerCompat.from(context).notify(notificationId, notification);
            } else {
                mRingtone.play();
            }
        }
    }
}

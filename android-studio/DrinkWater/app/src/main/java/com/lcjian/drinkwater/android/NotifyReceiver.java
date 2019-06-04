package com.lcjian.drinkwater.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.lcjian.drinkwater.BuildConfig;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.ui.Floating;
import com.lcjian.drinkwater.ui.home.MainActivity;
import com.lcjian.drinkwater.util.Utils;


public class NotifyReceiver extends BroadcastReceiver {

    public static final String ACTION_NOTIFY = "drink.water.ACTION_NOTIFY";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utils.isBackground(context)) { // App页面不处于前台时显示通知
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

                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(androidChannel);
            }

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
            remoteViews.setOnClickPendingIntent(
                    R.id.tv_drink,
                    PendingIntent.getActivity(
                            context,
                            1000,
                            new Intent(context, MainActivity.class)
                                    .putExtra("drunk_water", true)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                            PendingIntent.FLAG_ONE_SHOT));

            Notification notification = new NotificationCompat.Builder(context, BuildConfig.APPLICATION_ID)
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setCustomContentView(remoteViews)
                    .setSound(Uri.parse(("android.resource://" + context.getPackageName() + "/" + R.raw.water)))
                    .setContentIntent(PendingIntent.getActivity(
                            context,
                            1001,
                            new Intent(context, MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                            PendingIntent.FLAG_ONE_SHOT))
                    .setAutoCancel(true)
                    .build();
            NotificationManagerCompat.from(context).notify(1000, notification);
        }

        context.startActivity(new Intent(context, Floating.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

}

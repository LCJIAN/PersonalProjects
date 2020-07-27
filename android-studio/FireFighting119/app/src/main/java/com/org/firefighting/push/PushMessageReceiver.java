package com.org.firefighting.push;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class PushMessageReceiver extends JPushMessageReceiver {

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {
        String uri = null;
        if (!TextUtils.isEmpty(notificationMessage.notificationExtras)) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(notificationMessage.notificationExtras);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (jsonObject != null) {
                uri = jsonObject.optString("uri");
            }
        }

        if (!TextUtils.isEmpty(uri)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                super.onNotifyMessageOpened(context, notificationMessage);
            }
        } else {
            super.onNotifyMessageOpened(context, notificationMessage);
        }
    }
}

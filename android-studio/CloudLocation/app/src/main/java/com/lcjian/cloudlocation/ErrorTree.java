package com.lcjian.cloudlocation;

import android.text.TextUtils;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import androidx.annotation.NonNull;
import timber.log.Timber;

class ErrorTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        if (priority >= Log.ERROR) {
            if (t != null) {
                MobclickAgent.reportError(App.getInstance(), t);
            }
            if (!TextUtils.isEmpty(message)) {
                MobclickAgent.reportError(App.getInstance(), message);
            }
        }
    }
}

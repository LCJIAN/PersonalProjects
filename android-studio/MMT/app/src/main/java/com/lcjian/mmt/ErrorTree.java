package com.lcjian.mmt;

import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import androidx.annotation.NonNull;
import timber.log.Timber;

class ErrorTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        if (priority >= Log.ERROR && t != null) {
            MobclickAgent.reportError(App.getInstance(), t);
        }
    }
}

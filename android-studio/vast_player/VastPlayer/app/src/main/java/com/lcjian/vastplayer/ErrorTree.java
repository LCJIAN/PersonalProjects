package com.lcjian.vastplayer;

import android.util.Log;

import androidx.annotation.NonNull;

import com.umeng.analytics.MobclickAgent;

import timber.log.Timber;

class ErrorTree extends Timber.Tree {

    @Override
    protected void log(int priority, String tag, @NonNull String message, Throwable t) {
        if (priority >= Log.ERROR && t != null) {
            MobclickAgent.reportError(App.getInstance(), t);
        }
    }
}

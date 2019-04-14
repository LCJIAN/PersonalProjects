package com.lcjian.drinkwater.android;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotifyWorker extends Worker {

    public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        getApplicationContext().sendBroadcast(new Intent().setAction(NotifyService.NotifyReceiver.ACTION_NOTIFY));
        return Result.success();
    }

}
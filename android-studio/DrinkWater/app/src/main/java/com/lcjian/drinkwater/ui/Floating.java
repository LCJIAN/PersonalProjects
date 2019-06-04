package com.lcjian.drinkwater.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.android.NotifyWorker;
import com.lcjian.drinkwater.ui.base.BaseActivity;
import com.lcjian.drinkwater.ui.home.MainActivity;

import java.util.concurrent.TimeUnit;

public class Floating extends BaseActivity {

    private boolean mDrunkWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.floating_pop_view);

        findViewById(R.id.btn_drink).setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("drunk_water", true));
            mDrunkWater = true;
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        if (!mDrunkWater) {
            WorkManager.getInstance().enqueueUniqueWork(
                    "NotifyWorker",
                    ExistingWorkPolicy.REPLACE,
                    new OneTimeWorkRequest
                            .Builder(NotifyWorker.class)
                            .setInitialDelay(20, TimeUnit.MINUTES)
                            .build());
        }
        super.onDestroy();
    }
}

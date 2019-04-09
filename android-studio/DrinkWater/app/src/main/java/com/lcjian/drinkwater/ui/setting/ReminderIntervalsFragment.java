package com.lcjian.drinkwater.ui.setting;

import android.app.Dialog;
import android.os.Bundle;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Config;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class ReminderIntervalsFragment extends BaseDialogFragment {

    private Config mConfig;
    private Setting mSetting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = mAppDatabase.configDao().getAllSync().get(0);
        mSetting = mAppDatabase.settingDao().getAllSync().get(0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] arr = mConfig.reminderInterval.split(",");
        String[] arr2 = new String[arr.length];
        int index = 0;
        for (int i = 0; i < arr.length; i++) {
            if (mSetting.reminderInterval.equals(Integer.parseInt(arr[i]))) {
                index = i;
            }
            arr2[i] = getString(R.string.ef, arr[i]);
        }

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.fu)
                .setSingleChoiceItems(
                        arr2,
                        index,
                        (dialog, which) -> mSetting.reminderInterval = Integer.parseInt(arr[which]))
                .setNegativeButton(R.string.at,
                        (dialog, which) -> dismiss())
                .setPositiveButton(R.string.f4,
                        (dialog, which) -> {
                            mAppDatabase.settingDao().update(mSetting);
                            dismiss();
                        })
                .create();
    }
}

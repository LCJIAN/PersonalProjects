package com.lcjian.drinkwater.ui.setting;

import android.app.Dialog;
import android.os.Bundle;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class GendersFragment extends BaseDialogFragment {

    private Setting mSetting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetting = mAppDatabase.settingDao().getAllSync().get(0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] arr = new String[2];
        arr[0] = getString(R.string.male);
        arr[1] = getString(R.string.female);
        return new AlertDialog.Builder(getContext(), getTheme())
                .setTitle(R.string.da)
                .setSingleChoiceItems(
                        arr,
                        mSetting.gender.equals(0) ? 0 : 1,
                        (dialog, which) -> mSetting.gender = which)
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
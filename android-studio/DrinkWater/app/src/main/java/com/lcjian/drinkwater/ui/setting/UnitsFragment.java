package com.lcjian.drinkwater.ui.setting;

import android.app.Dialog;
import android.os.Bundle;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;
import com.lcjian.drinkwater.util.ComputeUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class UnitsFragment extends BaseDialogFragment {

    private List<Unit> mUnits;
    private Setting mSetting;
    private Unit mOldUnit;
    private Unit mNewUnit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUnits = mAppDatabase.unitDao().getAllSync();
        mSetting = mAppDatabase.settingDao().getAllSync().get(0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        List<String> unitNames = new ArrayList<>();
        mOldUnit = null;
        for (Unit unit : mUnits) {
            unitNames.add(unit.name);
            if (unit.id.equals(mSetting.unitId)) {
                mOldUnit = unit;
            }
        }
        String[] arr = new String[unitNames.size()];

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.i0)
                .setSingleChoiceItems(
                        unitNames.toArray(arr),
                        mOldUnit == null ? 0 : mUnits.indexOf(mOldUnit),
                        (dialog, which) -> mNewUnit = mUnits.get(which))
                .setNegativeButton(R.string.at,
                        (dialog, which) -> dismiss())
                .setPositiveButton(R.string.f4,
                        (dialog, which) -> {
                            if (mOldUnit != mNewUnit) {
                                double rate = 1;
                                if (mOldUnit != null) {
                                    rate = mOldUnit.rate;
                                }
                                mSetting.unitId = mNewUnit.id;
                                mSetting.weight = mNewUnit.rate / rate * mSetting.weight;
                                mSetting.intakeGoal = ComputeUtils.computeDailyRecommendIntakeGoal(mSetting.weight, mSetting.gender);
                                mAppDatabase.settingDao().update(mSetting);
                            }
                            dismiss();
                        })
                .create();
    }
}

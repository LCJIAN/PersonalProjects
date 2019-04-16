package com.lcjian.drinkwater.ui.setting;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;
import com.lcjian.drinkwater.util.ComputeUtils;
import com.lcjian.drinkwater.util.StringUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class WeightFragment extends BaseDialogFragment {

    private EditText et_weight;
    private TextView tv_unit;

    private Unit mCurrentUnit;
    private Setting mSetting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentUnit = mAppDatabase.unitDao().getCurrentUnitSync().get(0);
        mSetting = mAppDatabase.settingDao().getAllSync().get(0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_weight, null);
        et_weight = view.findViewById(R.id.et_weight);
        tv_unit = view.findViewById(R.id.tv_unit);

        setup();

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.iff)
                .setView(view)
                .setNegativeButton(R.string.at,
                        (dialog, which) -> dismiss())
                .setPositiveButton(R.string.f4,
                        (dialog, which) -> {
                            String s = et_weight.getEditableText().toString();
                            if (!TextUtils.isEmpty(s)) {
                                mSetting.weight = Double.parseDouble(s) / Double.parseDouble(mCurrentUnit.rate.split(",")[0]);
                                mSetting.intakeGoal = ComputeUtils.computeDailyRecommendIntakeGoal(mSetting.weight, mSetting.gender);
                                mAppDatabase.settingDao().update(mSetting);
                                dismiss();
                            }
                        })
                .create();
    }

    private void setup() {
        tv_unit.setText(mCurrentUnit.name.split(",")[0]);
        et_weight.setText(StringUtils.formatDecimalToString(mSetting.weight * Double.parseDouble(mCurrentUnit.rate.split(",")[0])));
    }
}

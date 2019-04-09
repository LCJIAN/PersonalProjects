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

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class WeightFragment extends BaseDialogFragment {

    private EditText et_weight;
    private TextView tv_unit;

    private List<Unit> mUnits;
    private Setting mSetting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUnits = mAppDatabase.unitDao().getAllSync();
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
                                mSetting.weight = Double.parseDouble(s);
                                mAppDatabase.settingDao().update(mSetting);
                                dismiss();
                            }
                        })
                .create();
    }

    private void setup() {
        Unit currentUnit = null;
        for (Unit unit : mUnits) {
            if (unit.id.equals(mSetting.unitId)) {
                currentUnit = unit;
            }
        }
        if (currentUnit != null) {
            tv_unit.setText(currentUnit.name.split(",")[0]);
        }
        et_weight.setText(String.valueOf(mSetting.weight));
    }
}

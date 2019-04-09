package com.lcjian.drinkwater.ui.setting;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;
import com.lcjian.drinkwater.util.ComputeUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class IntakeGoalFragment extends BaseDialogFragment {

    private TextView tv_intake_goal;
    private TextView tv_unit_for_intake_goal;
    private ImageButton btn_reset_recommend;
    private SeekBar sb_intake_goal;
    private TextView tv_recommend;
    private View v_recommend_indicator;

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
                .inflate(R.layout.fragment_intake_goal, null);
        tv_intake_goal = view.findViewById(R.id.tv_intake_goal);
        tv_unit_for_intake_goal = view.findViewById(R.id.tv_unit_for_intake_goal);
        btn_reset_recommend = view.findViewById(R.id.btn_reset_recommend);
        sb_intake_goal = view.findViewById(R.id.sb_intake_goal);
        tv_recommend = view.findViewById(R.id.tv_recommend);
        v_recommend_indicator = view.findViewById(R.id.v_recommend_indicator);

        btn_reset_recommend.setOnClickListener(v->{

        });
        setup();

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.a6)
                .setView(view)
                .setNegativeButton(R.string.at,
                        (dialog, which) -> dismiss())
                .setPositiveButton(R.string.f4,
                        (dialog, which) -> {
//                            String s = et_weight.getEditableText().toString();
//                            if (!TextUtils.isEmpty(s)) {
//                                mSetting.weight = Double.parseDouble(s);
//                                mAppDatabase.settingDao().update(mSetting);
//                                dismiss();
//                            }
                        })
                .create();
    }

    private void setup() {
        sb_intake_goal.post(() -> {
            sb_intake_goal.setMax(4500 - 800);
            sb_intake_goal.setProgress((int) (mSetting.intakeGoal - 800));

            double recommend = ComputeUtils.computeDailyRecommendIntakeGoal(mSetting.weight, mSetting.gender);
            v_recommend_indicator.setTranslationX((float) ((recommend - 800) / (4500 - 800) * sb_intake_goal.getWidth()));
            tv_recommend.setTranslationX((float) ((recommend - 800) / (4500 - 800) * sb_intake_goal.getWidth() - tv_recommend.getWidth() / 2d));

            Unit currentUnit = null;
            for (Unit unit : mUnits) {
                if (unit.id.equals(mSetting.unitId)) {
                    currentUnit = unit;
                }
            }
            if (currentUnit != null) {
                tv_unit_for_intake_goal.setText(currentUnit.name.split(",")[1]);
            }
            tv_intake_goal.setText(String.valueOf(mSetting.intakeGoal));
        });

    }
}

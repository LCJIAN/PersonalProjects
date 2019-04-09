package com.lcjian.drinkwater.ui.setting;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class ReminderModesFragment extends BaseDialogFragment implements View.OnClickListener {

    private TextView tv_mode_off;
    private TextView tv_mode_mute;
    private TextView tv_mode_auto;

    private Setting mSetting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetting = mAppDatabase.settingDao().getAllSync().get(0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_reminder_modes, null);
        tv_mode_off = view.findViewById(R.id.tv_mode_off);
        tv_mode_mute = view.findViewById(R.id.tv_mode_mute);
        tv_mode_auto = view.findViewById(R.id.tv_mode_auto);

        tv_mode_off.setOnClickListener(this);
        tv_mode_mute.setOnClickListener(this);
        tv_mode_auto.setOnClickListener(this);
        setup();

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setNegativeButton(R.string.at,
                        (dialog, which) -> dismiss())
                .setPositiveButton(R.string.f4,
                        (dialog, which) -> {
                            mAppDatabase.settingDao().update(mSetting);
                            dismiss();
                        })
                .create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_mode_off:
                mSetting.reminderMode = 0;
                break;
            case R.id.tv_mode_mute:
                mSetting.reminderMode = 1;
                break;
            case R.id.tv_mode_auto:
                mSetting.reminderMode = 2;
                break;
            default:
                break;
        }
        setup();
    }

    private void setup() {
        if (mSetting.reminderMode.equals(0)) {
            tv_mode_off.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mode_off_on, 0, 0);
            tv_mode_mute.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mode_mute_off, 0, 0);
            tv_mode_auto.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mode_auto_off, 0, 0);

            tv_mode_off.setTextColor(ContextCompat.getColor(tv_mode_off.getContext(), R.color.ax));
            tv_mode_mute.setTextColor(ContextCompat.getColor(tv_mode_mute.getContext(), R.color.ab));
            tv_mode_auto.setTextColor(ContextCompat.getColor(tv_mode_auto.getContext(), R.color.ab));
        } else if (mSetting.reminderMode.equals(1)) {
            tv_mode_off.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mode_off_off, 0, 0);
            tv_mode_mute.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mode_mute_on, 0, 0);
            tv_mode_auto.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mode_auto_off, 0, 0);

            tv_mode_off.setTextColor(ContextCompat.getColor(tv_mode_off.getContext(), R.color.ab));
            tv_mode_mute.setTextColor(ContextCompat.getColor(tv_mode_mute.getContext(), R.color.ax));
            tv_mode_auto.setTextColor(ContextCompat.getColor(tv_mode_auto.getContext(), R.color.ab));
        } else {
            tv_mode_off.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mode_off_off, 0, 0);
            tv_mode_mute.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mode_mute_off, 0, 0);
            tv_mode_auto.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mode_auto_on, 0, 0);

            tv_mode_off.setTextColor(ContextCompat.getColor(tv_mode_off.getContext(), R.color.ab));
            tv_mode_mute.setTextColor(ContextCompat.getColor(tv_mode_mute.getContext(), R.color.ab));
            tv_mode_auto.setTextColor(ContextCompat.getColor(tv_mode_auto.getContext(), R.color.ax));
        }
    }
}
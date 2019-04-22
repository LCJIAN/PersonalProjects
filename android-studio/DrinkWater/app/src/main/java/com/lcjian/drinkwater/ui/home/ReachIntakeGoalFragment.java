package com.lcjian.drinkwater.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ReachIntakeGoalFragment extends BaseDialogFragment {

    @BindView(R.id.tv_no)
    TextView tv_no;
    @BindView(R.id.tv_yes)
    TextView tv_yes;

    private Unbinder mUnBinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reach_intake_goal, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_no.setOnClickListener(v -> {
            Setting mSetting = mAppDatabase.settingDao().getAllSync().get(0);
            mSetting.furtherReminder = false;
            mAppDatabase.settingDao().update(mSetting);
            dismiss();
        });
        tv_yes.setOnClickListener(v -> {
            Setting mSetting = mAppDatabase.settingDao().getAllSync().get(0);
            mSetting.furtherReminder = true;
            mAppDatabase.settingDao().update(mSetting);
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }
}

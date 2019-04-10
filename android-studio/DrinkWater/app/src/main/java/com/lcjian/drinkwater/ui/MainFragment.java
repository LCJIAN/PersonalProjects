package com.lcjian.drinkwater.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.ui.base.BaseFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainFragment extends BaseFragment {

    @BindView(R.id.arc_progress)
    ArcProgress arc_progress;
    @BindView(R.id.tv_daily_intake_goal)
    TextView tv_daily_intake_goal;
    @BindView(R.id.iv_cup_type)
    ImageView iv_cup_type;
    @BindView(R.id.tv_cup_capacity)
    TextView tv_cup_capacity;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAppDatabase.settingDao().getAllAsync().map(settings -> settings.get(0))
                .subscribe();

    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
}

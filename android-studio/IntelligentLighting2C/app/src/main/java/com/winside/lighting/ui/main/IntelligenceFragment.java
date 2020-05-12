package com.winside.lighting.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winside.lighting.R;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.widget.RatioLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class IntelligenceFragment extends BaseFragment {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.rl_on_duty)
    RatioLayout rl_on_duty;
    @BindView(R.id.rl_off_duty)
    RatioLayout rl_off_duty;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intelligence, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_navigation_title.setText(R.string.action_intelligence);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
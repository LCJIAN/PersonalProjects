package com.lcjian.cloudlocation.ui.device;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HistoryPathSettingFragment extends BaseDialogFragment {

    @BindView(R.id.switch_lbs)
    Switch switch_lbs;
    @BindView(R.id.switch_follow)
    Switch switch_follow;
    @BindView(R.id.switch_line)
    Switch switch_line;
    @BindView(R.id.switch_dot)
    Switch switch_dot;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.tv_confirm)
    TextView tv_confirm;
    Unbinder unbinder;

    private boolean mLbs;
    private boolean mFollow;
    private boolean mLine;
    private boolean mDot;

    public static HistoryPathSettingFragment newInstance(boolean lbs,
                                                         boolean follow,
                                                         boolean line,
                                                         boolean dot) {
        HistoryPathSettingFragment fragment = new HistoryPathSettingFragment();
        Bundle args = new Bundle();
        args.putBoolean("lbs", lbs);
        args.putBoolean("follow", follow);
        args.putBoolean("line", line);
        args.putBoolean("dot", dot);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLbs = getArguments().getBoolean("lbs");
            mFollow = getArguments().getBoolean("follow");
            mLine = getArguments().getBoolean("line");
            mDot = getArguments().getBoolean("dot");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_path, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        switch_lbs.setChecked(mLbs);
        switch_follow.setChecked(mFollow);
        switch_line.setChecked(mLine);
        switch_dot.setChecked(mDot);

        tv_cancel.setOnClickListener(v -> dismiss());
        tv_confirm.setOnClickListener(v -> {
            mRxBus.send(new HistoryPathSettingEvent(
                    switch_lbs.isChecked(),
                    switch_follow.isChecked(),
                    switch_line.isChecked(),
                    switch_dot.isChecked()));
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    class HistoryPathSettingEvent {

        boolean lbs;
        boolean follow;
        boolean line;
        boolean dot;

        HistoryPathSettingEvent(boolean lbs, boolean follow, boolean line, boolean dot) {
            this.lbs = lbs;
            this.follow = follow;
            this.line = line;
            this.dot = dot;
        }
    }
}

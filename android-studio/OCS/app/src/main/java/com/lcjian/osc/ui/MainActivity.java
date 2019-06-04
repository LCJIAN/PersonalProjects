package com.lcjian.osc.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import android.widget.TextView;

import com.lcjian.osc.R;
import com.lcjian.osc.ui.base.BaseActivity;
import com.lcjian.osc.util.FragmentSwitchHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.bnv_main)
    BottomNavigationView bnv_main;

    private FragmentSwitchHelper mFragmentSwitchHelper;

    private int mCheckedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFragmentSwitchHelper = FragmentSwitchHelper.create(R.id.fl_fragment_container,
                getSupportFragmentManager(), true,
                new DetectionFragment(), new OverloadFragment());

        bnv_main.setOnNavigationItemSelectedListener(this);
        bnv_main.setSelectedItemId(R.id.action_detection);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int checkedId = item.getItemId();
        if (mCheckedId == checkedId) {
            return false;
        }
        switch (checkedId) {
            case R.id.action_detection: {
                tv_title.setText(R.string.action_detection);
                mFragmentSwitchHelper.changeFragment(DetectionFragment.class);
            }
            break;
            case R.id.action_overload: {
                tv_title.setText(R.string.action_overload);
                mFragmentSwitchHelper.changeFragment(OverloadFragment.class);
            }
            break;
            default:
                break;
        }
        mCheckedId = checkedId;
        return true;
    }
}

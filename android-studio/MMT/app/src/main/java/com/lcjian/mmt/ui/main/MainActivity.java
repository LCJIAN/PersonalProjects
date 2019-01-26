package com.lcjian.mmt.ui.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import com.lcjian.mmt.R;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.util.FragmentSwitchHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

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
                new QuoteManageFragment(), new LogisticsManageFragment(), new CarManageFragment(), new UserCenterFragment());

        bnv_main.setOnNavigationItemSelectedListener(this);
        bnv_main.setSelectedItemId(R.id.action_quote);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int checkedId = item.getItemId();
        if (mCheckedId == checkedId) {
            return false;
        }
        switch (checkedId) {
            case R.id.action_quote: {
                mFragmentSwitchHelper.changeFragment(QuoteManageFragment.class);
            }
            break;
            case R.id.action_logistics: {
                mFragmentSwitchHelper.changeFragment(LogisticsManageFragment.class);
            }
            break;
            case R.id.action_car: {
                mFragmentSwitchHelper.changeFragment(CarManageFragment.class);
            }
            break;
            case R.id.action_user: {
                mFragmentSwitchHelper.changeFragment(UserCenterFragment.class);
            }
            break;
            default:
                break;
        }
        mCheckedId = checkedId;
        return true;
    }
}

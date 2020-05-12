package com.winside.lighting.ui.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.winside.lighting.R;
import com.winside.lighting.util.FragmentSwitchHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

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
                new HomeFragment(), new ManagementFragment(), new SettingsFragment());

        bnv_main.setOnNavigationItemSelectedListener(this);
        bnv_main.setSelectedItemId(R.id.action_home);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int checkedId = item.getItemId();
        if (mCheckedId == checkedId) {
            return false;
        }
        switch (checkedId) {
            case R.id.action_home: {
                mFragmentSwitchHelper.changeFragment(HomeFragment.class);
            }
            break;
            case R.id.action_management: {
                mFragmentSwitchHelper.changeFragment(ManagementFragment.class);
            }
            break;
            case R.id.action_setting: {
                mFragmentSwitchHelper.changeFragment(SettingsFragment.class);
            }
            break;
            default:
                break;
        }
        mCheckedId = checkedId;
        return true;
    }
}

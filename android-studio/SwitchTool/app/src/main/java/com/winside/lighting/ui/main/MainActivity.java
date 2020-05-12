package com.winside.lighting.ui.main;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.winside.lighting.GlideApp;
import com.winside.lighting.R;
import com.winside.lighting.util.FragmentSwitchHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.iv_background)
    ImageView iv_background;
    @BindView(R.id.bnv_main)
    BottomNavigationView bnv_main;

    private FragmentSwitchHelper mFragmentSwitchHelper;

    private int mCheckedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        GlideApp.with(this)
                .load(R.drawable.background)
                .centerCrop()
                .into(iv_background);

        mFragmentSwitchHelper = FragmentSwitchHelper.create(R.id.fl_fragment_container,
                getSupportFragmentManager(), true, new DevicesFragment(), new GroupsFragment());

        bnv_main.setOnNavigationItemSelectedListener(this);
        bnv_main.setSelectedItemId(R.id.action_device);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int checkedId = item.getItemId();
        if (mCheckedId == checkedId) {
            return false;
        }
        switch (checkedId) {
            case R.id.action_device: {
                mFragmentSwitchHelper.changeFragment(DevicesFragment.class);
            }
            break;
            case R.id.action_group: {
                mFragmentSwitchHelper.changeFragment(GroupsFragment.class);
            }
            break;
            default:
                break;
        }
        mCheckedId = checkedId;
        return true;
    }
}

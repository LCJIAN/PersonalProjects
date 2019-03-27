package com.lcjian.mmt.ui.user;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.lcjian.mmt.R;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.SimpleFragmentPagerAdapter;

import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SignInUpActivity extends BaseActivity {

    @BindView(R.id.tab_sign)
    TabLayout tab_sign;
    @BindView(R.id.vp_sign)
    ViewPager vp_sign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_up);
        ButterKnife.bind(this);

        vp_sign.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager())
                .addFragment(new SignInFragment(), getString(R.string.sign_in))
                .addFragment(new SignUpFragment(), getString(R.string.sign_up)));
        tab_sign.setupWithViewPager(vp_sign);
    }
}

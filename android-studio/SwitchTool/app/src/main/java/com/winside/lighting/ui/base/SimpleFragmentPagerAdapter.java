package com.winside.lighting.ui.base;

import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragments;
    private final List<String> mFragmentTitles;

    public SimpleFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        this.mFragments = new ArrayList<>();
        this.mFragmentTitles = new ArrayList<>();
    }

    public SimpleFragmentPagerAdapter addFragment(Fragment fragment, String title) {
        mFragments.add(fragment);
        mFragmentTitles.add(title);
        return this;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TextUtils.isEmpty(mFragmentTitles.get(position)) ? super.getPageTitle(position) : mFragmentTitles.get(position);
    }
}
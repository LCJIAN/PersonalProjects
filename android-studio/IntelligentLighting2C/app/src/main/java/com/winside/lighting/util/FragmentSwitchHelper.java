package com.winside.lighting.util;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentSwitchHelper {

    private static final String TAG = "FragmentSwitchHelper";

    private FragmentManager mFragmentManager;

    private Fragment[] mFragments;

    private Fragment mCurrentFragment;

    private int mContainerId;

    private boolean mUseHide;

    private FragmentSwitchHelper(int containerId,
                                 FragmentManager fragmentManager, boolean useHide, Fragment... fragments) {
        this.mContainerId = containerId;
        this.mFragments = fragments;
        this.mFragmentManager = fragmentManager;
        this.mUseHide = useHide;
    }

    public static FragmentSwitchHelper create(int containerId,
                                              FragmentManager fragmentManager, boolean useHide, Fragment... fragments) {
        return new FragmentSwitchHelper(containerId, fragmentManager, useHide, fragments);
    }

    public void changeFragment(Class<? extends Fragment> replaceFragmentClass) {
        if (!mFragmentManager.isDestroyed()) {
            Fragment replaceFragment = mFragmentManager.findFragmentByTag(replaceFragmentClass.getName());
            if (mCurrentFragment == replaceFragment && mCurrentFragment != null) {
                Log.d(TAG, "changeFragment noChange ");
                return;
            }
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            if (mCurrentFragment != null) {
                if (mUseHide) {
                    fragmentTransaction.hide(mCurrentFragment);
                } else {
                    fragmentTransaction.detach(mCurrentFragment);
                }
                Log.d(TAG, "changeFragment detach " + mCurrentFragment.getClass().getName());
            }
            if (replaceFragment == null) {
                for (Fragment item : mFragments) {
                    if (item.getClass().getName().equals(replaceFragmentClass.getName())) {
                        replaceFragment = item;
                        break;
                    }
                }
                fragmentTransaction.add(mContainerId, replaceFragment, replaceFragmentClass.getName());
                mCurrentFragment = replaceFragment;
                Log.d(TAG, "changeFragment add " + replaceFragmentClass.getName());
            } else {
                if (mUseHide) {
                    fragmentTransaction.show(replaceFragment);
                } else {
                    fragmentTransaction.attach(replaceFragment);
                }
                mCurrentFragment = replaceFragment;
                Log.d(TAG, "changeFragment attach " + replaceFragmentClass.getName());
            }
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    @SuppressLint("unchecked")
    public <T extends Fragment> T findFragment(Class<T> fragmentClass) {
        return (T) mFragmentManager.findFragmentByTag(fragmentClass.getName());
    }
}

package com.lcjian.lib;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class RetainViewFragment extends Fragment {

    private View mContentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mContentView == null) {
            mContentView = inflater.inflate(getContentResource(), container, false);
            onCreateView(mContentView, savedInstanceState);
        } else {
            ((ViewGroup) mContentView.getParent()).removeView(mContentView);
        }
        return mContentView;
    }

    public abstract int getContentResource();

    public abstract void onCreateView(View contentView, Bundle savedInstanceState);
}

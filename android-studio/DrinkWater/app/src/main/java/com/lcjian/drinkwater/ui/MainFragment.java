package com.lcjian.drinkwater.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.ui.base.BaseFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MainFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        return view;
    }
}

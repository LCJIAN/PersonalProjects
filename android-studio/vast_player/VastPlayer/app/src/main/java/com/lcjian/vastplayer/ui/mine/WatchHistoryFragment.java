package com.lcjian.vastplayer.ui.mine;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.lcjian.lib.content.SimpleFragmentPagerAdapter;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class WatchHistoryFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_watch_history)
    TabLayout tab_watch_history;
    @BindView(R.id.vp_watch_history)
    ViewPager vp_watch_history;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch_history, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        toolbar.setTitle(R.string.action_watch_history);
        mRxBus.send(toolbar);
        SimpleFragmentPagerAdapter pagerAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager());
        pagerAdapter.addFragment(WatchHistorySubjectsFragment.newInstance("movie"), getString(R.string.action_movie));
        pagerAdapter.addFragment(WatchHistorySubjectsFragment.newInstance("tv_show"), getString(R.string.action_tv_show));
        pagerAdapter.addFragment(WatchHistorySubjectsFragment.newInstance("variety"), getString(R.string.action_variety));
        pagerAdapter.addFragment(WatchHistorySubjectsFragment.newInstance("animation"), getString(R.string.action_animation));
        vp_watch_history.setAdapter(pagerAdapter);
        tab_watch_history.setupWithViewPager(vp_watch_history);
        tab_watch_history.setSelectedTabIndicatorColor(Color.WHITE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

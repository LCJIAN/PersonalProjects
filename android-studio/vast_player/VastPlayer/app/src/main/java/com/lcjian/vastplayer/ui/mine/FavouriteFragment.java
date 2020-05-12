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

public class FavouriteFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tab_favourite)
    TabLayout tab_favourite;
    @BindView(R.id.vp_favourite)
    ViewPager vp_favourite;

    private Unbinder mUnBinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        toolbar.setTitle(R.string.action_favorite);
        mRxBus.send(toolbar);
        SimpleFragmentPagerAdapter pagerAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager());
        pagerAdapter.addFragment(FavouriteSubjectsFragment.newInstance("movie"), getString(R.string.action_movie));
        pagerAdapter.addFragment(FavouriteSubjectsFragment.newInstance("tv_show"), getString(R.string.action_tv_show));
        pagerAdapter.addFragment(FavouriteSubjectsFragment.newInstance("variety"), getString(R.string.action_variety));
        pagerAdapter.addFragment(FavouriteSubjectsFragment.newInstance("animation"), getString(R.string.action_animation));
        pagerAdapter.addFragment(new FavouriteTvStationFragment(), getString(R.string.action_live_tv));
        vp_favourite.setAdapter(pagerAdapter);
        tab_favourite.setupWithViewPager(vp_favourite);
        tab_favourite.setSelectedTabIndicatorColor(Color.WHITE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }
}

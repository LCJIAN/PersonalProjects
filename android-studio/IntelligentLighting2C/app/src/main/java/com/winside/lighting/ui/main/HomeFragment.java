package com.winside.lighting.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.winside.lighting.R;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.base.SimpleFragmentPagerAdapter;
import com.winside.lighting.widget.AutoViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends BaseFragment {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.vp_banner)
    AutoViewPager vp_banner;
    @BindView(R.id.vpi_banner)
    SmartTabLayout vpi_banner;
    @BindView(R.id.tab_device)
    TabLayout tab_device;
    @BindView(R.id.vp_device)
    ViewPager vp_device;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_navigation_title.setText(R.string.action_home);

        vp_banner.setAdapter(new BannerAdapter(Arrays.asList(R.drawable.banner_1, R.drawable.banner_2, R.drawable.banner_3, R.drawable.banner_4)));
        vp_banner.setOffscreenPageLimit(2);
        vpi_banner.setViewPager(vp_banner);

        tab_device.setupWithViewPager(vp_device);
        vp_device.setAdapter(new SimpleFragmentPagerAdapter(getChildFragmentManager())
                .addFragment(new DevicesFragment(), "设备")
                .addFragment(new RegionsFragment(), "区域"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    class BannerAdapter extends PagerAdapter {

        private List<Integer> mData;

        private List<View> mRecycledViews;

        BannerAdapter(List<Integer> data) {
            this.mData = data;
            this.mRecycledViews = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view;
            if (mRecycledViews.isEmpty()) {
                view = LayoutInflater.from(container.getContext()).inflate(R.layout.banner_item, container, false);
            } else {
                view = mRecycledViews.get(0);
                mRecycledViews.remove(0);
            }
            final Integer res = mData.get(position);

            ImageView iv_banner_image = view.findViewById(R.id.iv_banner_image);
            Glide.with(container.getContext())
                    .load(res)
                    .apply(RequestOptions.centerCropTransform())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv_banner_image);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
            mRecycledViews.add((View) object);
        }
    }
}

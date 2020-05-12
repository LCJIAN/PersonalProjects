package com.lcjian.vastplayer.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.Recommend;
import com.lcjian.vastplayer.ui.subject.MovieActivity;

import java.util.ArrayList;
import java.util.List;

class RecommendBannerAdapter extends PagerAdapter {

    private List<Recommend> mData;

    private List<View> mRecycledViews;

    RecommendBannerAdapter(List<Recommend> data) {
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
            view = LayoutInflater.from(container.getContext()).inflate(R.layout.recommend_banner_item, container, false);
        } else {
            view = mRecycledViews.get(0);
            mRecycledViews.remove(0);
        }
        final Recommend recommend = mData.get(position);

        ImageView iv_banner_image = view.findViewById(R.id.iv_banner_image);
        TextView tv_banner_title = view.findViewById(R.id.tv_banner_title);
        tv_banner_title.setText(recommend.extra);
        Glide.with(container.getContext())
                .load(recommend.extra1)
                .apply(RequestOptions.placeholderOf(R.drawable.color_video_place_holder).centerCrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_banner_image);

        view.setOnClickListener(v -> v.getContext().startActivity(
                new Intent(v.getContext(), MovieActivity.class)
                        .putExtra(Constants.BUNDLE_PARAMETER_SUBJECT, recommend.convertedData)));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        mRecycledViews.add((View) object);
    }
}

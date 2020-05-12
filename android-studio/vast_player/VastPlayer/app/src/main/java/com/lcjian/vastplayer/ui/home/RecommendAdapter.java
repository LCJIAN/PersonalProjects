package com.lcjian.vastplayer.ui.home;

import android.content.Intent;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.lib.viewpager.AutoViewPager;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.Recommend;
import com.lcjian.vastplayer.ui.subject.SubjectsActivity;
import com.lcjian.vastplayer.ui.subject.TvStationsActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class RecommendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NORMAL = 1;

    private List<List<Recommend>> mData;

    RecommendAdapter(List<List<Recommend>> data) {
        this.mData = data;
    }

    @Override
    public int getItemViewType(int position) {
        return TextUtils.equals(mData.get(position).get(0).title, "banner") ? TYPE_HEADER : TYPE_NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == TYPE_HEADER ? new RecommendAdapter.RecommendHeaderViewHolder(parent) : new RecommendAdapter.RecommendViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecommendAdapter.RecommendHeaderViewHolder) {
            ((RecommendAdapter.RecommendHeaderViewHolder) holder).bindTo(mData.get(position));
        } else if (holder instanceof RecommendAdapter.RecommendViewHolder) {
            ((RecommendAdapter.RecommendViewHolder) holder).bindTo(mData.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    static class RecommendHeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vp_banner)
        AutoViewPager vp_banner;
        @BindView(R.id.vpi_banner)
        SmartTabLayout vpi_banner;
        @BindView(R.id.tv_go_movies)
        TextView tv_go_movies;
        @BindView(R.id.tv_go_tv_shows)
        TextView tv_go_tv_shows;
        @BindView(R.id.tv_go_variety)
        TextView tv_go_variety;
        @BindView(R.id.tv_go_animation)
        TextView tv_go_animation;
        @BindView(R.id.tv_go_live)
        TextView tv_go_live;

        List<Recommend> recommends;

        RecommendHeaderViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_header, parent, false));
            ButterKnife.bind(this, this.itemView);

            View.OnClickListener onClickListener = view -> {
                switch (view.getId()) {
                    case R.id.tv_go_movies:
                        view.getContext().startActivity(new Intent(view.getContext(), SubjectsActivity.class).putExtra("type", "movie"));
                        break;
                    case R.id.tv_go_tv_shows:
                        view.getContext().startActivity(new Intent(view.getContext(), SubjectsActivity.class).putExtra("type", "tv_show"));
                        break;
                    case R.id.tv_go_variety:
                        view.getContext().startActivity(new Intent(view.getContext(), SubjectsActivity.class).putExtra("type", "variety"));
                        break;
                    case R.id.tv_go_animation:
                        view.getContext().startActivity(new Intent(view.getContext(), SubjectsActivity.class).putExtra("type", "animation"));
                        break;
                    case R.id.tv_go_live:
                        view.getContext().startActivity(new Intent(view.getContext(), TvStationsActivity.class));
                        break;
                    default:
                        break;
                }
            };
            tv_go_movies.setOnClickListener(onClickListener);
            tv_go_tv_shows.setOnClickListener(onClickListener);
            tv_go_variety.setOnClickListener(onClickListener);
            tv_go_animation.setOnClickListener(onClickListener);
            tv_go_live.setOnClickListener(onClickListener);
        }

        void bindTo(List<Recommend> recommends) {
            this.recommends = recommends;

            vp_banner.setAdapter(new RecommendBannerAdapter(recommends));
            vp_banner.setOffscreenPageLimit(2);
            vpi_banner.setViewPager(vp_banner);
        }
    }

    static class RecommendViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_recommend_title)
        TextView tv_recommend_title;
        @BindView(R.id.rv_recommend_list)
        RecyclerView rv_recommend_list;

        List<Recommend> recommends;

        RecommendViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_item, parent, false));
            ButterKnife.bind(this, this.itemView);

            rv_recommend_list.setHasFixedSize(true);
            rv_recommend_list.setNestedScrollingEnabled(false);
            rv_recommend_list.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rv_recommend_list.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    int position = parent.getChildAdapterPosition(view);
                    if (position == RecyclerView.NO_POSITION) {
                        super.getItemOffsets(outRect, view, parent, state);
                    }
                    int offsets1 = (int) DimenUtils.dipToPixels(4, parent.getContext());
                    int offsets2 = (int) DimenUtils.dipToPixels(8, parent.getContext());
                    outRect.set(offsets2, offsets1, 0, offsets1);
                }
            });
            new GravitySnapHelper(Gravity.START).attachToRecyclerView(rv_recommend_list);
        }

        void bindTo(List<Recommend> recommends) {
            this.recommends = recommends;
            String title = recommends.get(0).title;
            if (TextUtils.equals(recommends.get(0).title, "recent")) {
                title = itemView.getContext().getString(R.string.recent);
            }
            if (TextUtils.equals(recommends.get(0).title, "classic")) {
                title = itemView.getContext().getString(R.string.classic);
            }
            if (TextUtils.equals(recommends.get(0).title, "popular")) {
                title = itemView.getContext().getString(R.string.popular);
            }
            String type = itemView.getContext().getString(R.string.action_micro_video);
            if (TextUtils.equals(recommends.get(0).type, "movie")) {
                type = itemView.getContext().getString(R.string.movies);
            }
            if (TextUtils.equals(recommends.get(0).type, "tv_show")) {
                type = itemView.getContext().getString(R.string.tv_shows);
            }
            if (TextUtils.equals(recommends.get(0).type, "variety")) {
                type = itemView.getContext().getString(R.string.variety);
            }
            if (TextUtils.equals(recommends.get(0).type, "animation")) {
                type = itemView.getContext().getString(R.string.animation);
            }

            title = title + type;
            tv_recommend_title.setText(title);
            rv_recommend_list.setAdapter(new RecommendSubjectAdapter(recommends));
        }
    }
}

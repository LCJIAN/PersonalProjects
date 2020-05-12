package com.lcjian.vastplayer.ui.home;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.Global;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.Recommend;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.ui.subject.MovieActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class RecommendSubjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SUBJECT = 0;
    private static final int TYPE_MICRO_VIDEO = 1;

    private List<Recommend> mData;

    RecommendSubjectAdapter(List<Recommend> data) {
        this.mData = data;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TextUtils.equals("video", mData.get(position).convertedData.type) ? TYPE_MICRO_VIDEO : TYPE_SUBJECT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == TYPE_MICRO_VIDEO ? new RecommendMicroVideoViewHolder(parent) : new RecommendSubjectViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecommendSubjectViewHolder) {
            ((RecommendSubjectViewHolder) holder).bindTo(mData.get(position));
        } else if (holder instanceof RecommendMicroVideoViewHolder) {
            ((RecommendMicroVideoViewHolder) holder).bindTo(mData.get(position));
        }
    }

    static class RecommendSubjectViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_subject_poster)
        ImageView iv_subject_poster;
        @BindView(R.id.tv_subject_title)
        TextView tv_subject_title;

        Recommend recommend;

        RecommendSubjectViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_subject_item, parent, false));
            ButterKnife.bind(this, this.itemView);

            itemView.setOnClickListener(v -> v.getContext().startActivity(
                    new Intent(v.getContext(), MovieActivity.class)
                            .putExtra(Constants.BUNDLE_PARAMETER_SUBJECT, recommend.convertedData)));
        }

        void bindTo(Recommend recommend) {
            this.recommend = recommend;
            Subject subject = recommend.convertedData;
            if (subject.posters != null && !subject.posters.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(subject.posters.get(0).url)
                        .apply(Global.roundedPoster)
                        .transition(Global.dontTransition)
                        .into(iv_subject_poster);
            }
            tv_subject_title.setText(subject.title);
        }
    }

    static class RecommendMicroVideoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_micro_video_thumbnail)
        ImageView iv_micro_video_thumbnail;
        @BindView(R.id.tv_micro_video_title)
        TextView tv_micro_video_title;

        Recommend recommend;

        RecommendMicroVideoViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_micro_video_item, parent, false));
            ButterKnife.bind(this, this.itemView);

            itemView.setOnClickListener(v -> v.getContext().startActivity(
                    new Intent(v.getContext(), MovieActivity.class)
                            .putExtra(Constants.BUNDLE_PARAMETER_SUBJECT, recommend.convertedData)));
        }

        void bindTo(Recommend recommend) {
            this.recommend = recommend;
            Subject subject = recommend.convertedData;
            if (subject.thumbnails != null && !subject.thumbnails.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(subject.thumbnails.get(0).url)
                        .apply(Global.centerCrop)
                        .transition(Global.crossFade)
                        .into(iv_micro_video_thumbnail);
            }
            tv_micro_video_title.setText(subject.title);
        }
    }
}

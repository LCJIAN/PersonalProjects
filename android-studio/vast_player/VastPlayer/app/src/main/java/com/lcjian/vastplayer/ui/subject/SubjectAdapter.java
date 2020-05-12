package com.lcjian.vastplayer.ui.subject;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.florent37.glidepalette.GlidePalette;
import com.lcjian.lib.util.common.AnimationUtils;
import com.lcjian.lib.util.common.ColorUtils;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.Subject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MOVIE = 0;
    private static final int TYPE_TV_SHOW = 1;
    private static final int TYPE_VARIETY = 2;
    private static final int TYPE_ANIMATION = 3;

    private List<Subject> mSubjects;

    public SubjectAdapter(List<Subject> subjects) {
        this.mSubjects = subjects;
    }

    public void replaceAll(final List<Subject> subjects) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {

            @Override
            public int getOldListSize() {
                return mSubjects == null ? 0 : mSubjects.size();
            }

            @Override
            public int getNewListSize() {
                return subjects == null ? 0 : subjects.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return mSubjects.get(oldItemPosition).id.equals(subjects.get(newItemPosition).id);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }
        }, true);
        mSubjects = subjects;
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        return mSubjects == null ? 0 : mSubjects.size();
    }

    @Override
    public int getItemViewType(int position) {
        String type = mSubjects.get(position).type;
        switch (type) {
            case "movie":
                return TYPE_MOVIE;
            case "tv_show":
                return TYPE_TV_SHOW;
            case "variety":
                return TYPE_VARIETY;
            case "animation":
                return TYPE_ANIMATION;
            default:
                return TYPE_MOVIE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_MOVIE:
                return new MovieViewHolder(parent);
            case TYPE_TV_SHOW:
                return new TvShowViewHolder(parent);
            case TYPE_VARIETY:
                return new VarietyViewHolder(parent);
            case TYPE_ANIMATION:
                return new AnimationViewHolder(parent);
            default:
                return new MovieViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MovieViewHolder) {
            ((MovieViewHolder) holder).bindTo(mSubjects.get(position));
        } else if (holder instanceof TvShowViewHolder) {
            ((TvShowViewHolder) holder).bindTo(mSubjects.get(position));
        } else if (holder instanceof VarietyViewHolder) {
            ((VarietyViewHolder) holder).bindTo(mSubjects.get(position));
        } else if (holder instanceof AnimationViewHolder) {
            ((AnimationViewHolder) holder).bindTo(mSubjects.get(position));
        }
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_movie_poster)
        ImageView iv_movie_poster;
        @BindView(R.id.rl_movie_info)
        RelativeLayout rl_movie_info;
        @BindView(R.id.tv_movie_title)
        TextView tv_movie_title;
        @BindView(R.id.tv_movie_release_date)
        TextView tv_movie_release_date;
        @BindView(R.id.tv_movie_vote_average)
        TextView tv_movie_vote_average;

        Subject subject;

        MovieViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false));
            ButterKnife.bind(this, this.itemView);
            itemView.setOnClickListener(v -> v.getContext().startActivity(
                    new Intent(v.getContext(), MovieActivity.class)
                            .putExtra(Constants.BUNDLE_PARAMETER_SUBJECT, subject)));
        }

        void bindTo(Subject subject) {
            this.subject = subject;
            Context context = itemView.getContext();
            rl_movie_info.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_800));
            tv_movie_title.setTextColor(ContextCompat.getColor(context, R.color.primary_text_light));
            tv_movie_release_date.setTextColor(ContextCompat.getColor(context, R.color.secondary_text_light));
            tv_movie_vote_average.setTextColor(ContextCompat.getColor(context, R.color.secondary_text_light));

            String poster = subject.posters == null || subject.posters.isEmpty() ? "" : subject.posters.get(0).url;
            Glide.with(context)
                    .load(poster)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_movie).centerCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(GlidePalette.with(poster).intoCallBack(palette -> {
                        if (palette != null) {
                            Palette.Swatch swatch = ColorUtils.getMostPopulousSwatch(palette);
                            if (swatch != null) {
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.grey_800);
                                    int endColor = swatch.getRgb();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateBackgroundColorChange(rl_movie_info, startColor, endColor);
                                    } else {
                                        rl_movie_info.setBackgroundColor(endColor);
                                    }
                                }
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_text_light);
                                    int endColor = swatch.getTitleTextColor();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateTextColorChange(tv_movie_title, startColor, endColor);
                                    } else {
                                        tv_movie_title.setTextColor(endColor);
                                    }
                                }
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_text_light);
                                    int endColor = swatch.getBodyTextColor();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateTextColorChange(tv_movie_release_date, startColor, endColor);
                                        AnimationUtils.animateTextColorChange(tv_movie_vote_average, startColor, endColor);
                                    } else {
                                        tv_movie_release_date.setTextColor(endColor);
                                        tv_movie_vote_average.setTextColor(endColor);
                                    }
                                }
                            }
                        }
                    }))
                    .into(iv_movie_poster);
            tv_movie_title.setText(subject.title);
            tv_movie_release_date.setText(DateUtils.convertDateToStr(subject.release_date, "yyyy"));
            tv_movie_vote_average.setText(String.valueOf(subject.vote_average));
        }
    }

    static class TvShowViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_tv_show_poster)
        ImageView iv_tv_show_poster;
        @BindView(R.id.rl_tv_show_info)
        RelativeLayout rl_tv_show_info;
        @BindView(R.id.tv_tv_show_title)
        TextView tv_tv_show_title;
        @BindView(R.id.tv_tv_show_release_date)
        TextView tv_tv_show_release_date;
        @BindView(R.id.tv_tv_show_vote_average)
        TextView tv_tv_show_vote_average;

        Subject subject;

        TvShowViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_show_item, parent, false));
            ButterKnife.bind(this, this.itemView);
            itemView.setOnClickListener(v -> v.getContext().startActivity(
                    new Intent(v.getContext(), MovieActivity.class)
                            .putExtra(Constants.BUNDLE_PARAMETER_SUBJECT, subject)));
        }

        void bindTo(Subject subject) {
            this.subject = subject;
            Context context = itemView.getContext();
            rl_tv_show_info.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_800));
            tv_tv_show_title.setTextColor(ContextCompat.getColor(context, R.color.primary_text_light));
            tv_tv_show_release_date.setTextColor(ContextCompat.getColor(context, R.color.secondary_text_light));
            tv_tv_show_vote_average.setTextColor(ContextCompat.getColor(context, R.color.secondary_text_light));

            String poster = subject.posters == null || subject.posters.isEmpty() ? "" : subject.posters.get(0).url;
            Glide.with(context)
                    .load(poster)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_movie).centerCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(GlidePalette.with(poster).intoCallBack(palette -> {
                        if (palette != null) {
                            Palette.Swatch swatch = ColorUtils.getMostPopulousSwatch(palette);
                            if (swatch != null) {
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.grey_800);
                                    int endColor = swatch.getRgb();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateBackgroundColorChange(rl_tv_show_info, startColor, endColor);
                                    } else {
                                        rl_tv_show_info.setBackgroundColor(endColor);
                                    }
                                }
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_text_light);
                                    int endColor = swatch.getTitleTextColor();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateTextColorChange(tv_tv_show_title, startColor, endColor);
                                    } else {
                                        tv_tv_show_title.setTextColor(endColor);
                                    }
                                }
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_text_light);
                                    int endColor = swatch.getBodyTextColor();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateTextColorChange(tv_tv_show_release_date, startColor, endColor);
                                        AnimationUtils.animateTextColorChange(tv_tv_show_vote_average, startColor, endColor);
                                    } else {
                                        tv_tv_show_release_date.setTextColor(endColor);
                                        tv_tv_show_vote_average.setTextColor(endColor);
                                    }
                                }
                            }
                        }
                    }))
                    .into(iv_tv_show_poster);
            tv_tv_show_title.setText(subject.title);
            tv_tv_show_release_date.setText(DateUtils.convertDateToStr(subject.release_date, "yyyy"));
            tv_tv_show_vote_average.setText(String.valueOf(subject.vote_average));
        }
    }

    static class VarietyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_tv_show_poster)
        ImageView iv_tv_show_poster;
        @BindView(R.id.rl_tv_show_info)
        RelativeLayout rl_tv_show_info;
        @BindView(R.id.tv_tv_show_title)
        TextView tv_tv_show_title;
        @BindView(R.id.tv_tv_show_release_date)
        TextView tv_tv_show_release_date;
        @BindView(R.id.tv_tv_show_vote_average)
        TextView tv_tv_show_vote_average;

        Subject subject;

        VarietyViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_show_item, parent, false));
            ButterKnife.bind(this, this.itemView);
            itemView.setOnClickListener(v -> v.getContext().startActivity(
                    new Intent(v.getContext(), MovieActivity.class)
                            .putExtra(Constants.BUNDLE_PARAMETER_SUBJECT, subject)));
        }

        void bindTo(Subject subject) {
            this.subject = subject;
            Context context = itemView.getContext();
            rl_tv_show_info.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_800));
            tv_tv_show_title.setTextColor(ContextCompat.getColor(context, R.color.primary_text_light));
            tv_tv_show_release_date.setTextColor(ContextCompat.getColor(context, R.color.secondary_text_light));
            tv_tv_show_vote_average.setTextColor(ContextCompat.getColor(context, R.color.secondary_text_light));

            String poster = subject.posters == null || subject.posters.isEmpty() ? "" : subject.posters.get(0).url;
            Glide.with(context)
                    .load(poster)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_movie).centerCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(GlidePalette.with(poster).intoCallBack(palette -> {
                        if (palette != null) {
                            Palette.Swatch swatch = ColorUtils.getMostPopulousSwatch(palette);
                            if (swatch != null) {
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.grey_800);
                                    int endColor = swatch.getRgb();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateBackgroundColorChange(rl_tv_show_info, startColor, endColor);
                                    } else {
                                        rl_tv_show_info.setBackgroundColor(endColor);
                                    }
                                }
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_text_light);
                                    int endColor = swatch.getTitleTextColor();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateTextColorChange(tv_tv_show_title, startColor, endColor);
                                    } else {
                                        tv_tv_show_title.setTextColor(endColor);
                                    }
                                }
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_text_light);
                                    int endColor = swatch.getBodyTextColor();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateTextColorChange(tv_tv_show_release_date, startColor, endColor);
                                        AnimationUtils.animateTextColorChange(tv_tv_show_vote_average, startColor, endColor);
                                    } else {
                                        tv_tv_show_release_date.setTextColor(endColor);
                                        tv_tv_show_vote_average.setTextColor(endColor);
                                    }
                                }
                            }
                        }
                    }))
                    .into(iv_tv_show_poster);
            tv_tv_show_title.setText(subject.title);
            tv_tv_show_release_date.setText(DateUtils.convertDateToStr(subject.release_date, "yyyy"));
            tv_tv_show_vote_average.setText(String.valueOf(subject.vote_average));
        }
    }

    static class AnimationViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_tv_show_poster)
        ImageView iv_tv_show_poster;
        @BindView(R.id.rl_tv_show_info)
        RelativeLayout rl_tv_show_info;
        @BindView(R.id.tv_tv_show_title)
        TextView tv_tv_show_title;
        @BindView(R.id.tv_tv_show_release_date)
        TextView tv_tv_show_release_date;
        @BindView(R.id.tv_tv_show_vote_average)
        TextView tv_tv_show_vote_average;

        Subject subject;

        AnimationViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_show_item, parent, false));
            ButterKnife.bind(this, this.itemView);
            itemView.setOnClickListener(v -> v.getContext().startActivity(
                    new Intent(v.getContext(), MovieActivity.class)
                            .putExtra(Constants.BUNDLE_PARAMETER_SUBJECT, subject)));
        }

        void bindTo(Subject subject) {
            this.subject = subject;
            Context context = itemView.getContext();
            rl_tv_show_info.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_800));
            tv_tv_show_title.setTextColor(ContextCompat.getColor(context, R.color.primary_text_light));
            tv_tv_show_release_date.setTextColor(ContextCompat.getColor(context, R.color.secondary_text_light));
            tv_tv_show_vote_average.setTextColor(ContextCompat.getColor(context, R.color.secondary_text_light));

            String poster = subject.posters == null || subject.posters.isEmpty() ? "" : subject.posters.get(0).url;
            Glide.with(context)
                    .load(poster)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_movie).centerCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(GlidePalette.with(poster).intoCallBack(palette -> {
                        if (palette != null) {
                            Palette.Swatch swatch = ColorUtils.getMostPopulousSwatch(palette);
                            if (swatch != null) {
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.grey_800);
                                    int endColor = swatch.getRgb();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateBackgroundColorChange(rl_tv_show_info, startColor, endColor);
                                    } else {
                                        rl_tv_show_info.setBackgroundColor(endColor);
                                    }
                                }
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_text_light);
                                    int endColor = swatch.getTitleTextColor();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateTextColorChange(tv_tv_show_title, startColor, endColor);
                                    } else {
                                        tv_tv_show_title.setTextColor(endColor);
                                    }
                                }
                                {
                                    int startColor = ContextCompat.getColor(itemView.getContext(), R.color.primary_text_light);
                                    int endColor = swatch.getBodyTextColor();

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        AnimationUtils.animateTextColorChange(tv_tv_show_release_date, startColor, endColor);
                                        AnimationUtils.animateTextColorChange(tv_tv_show_vote_average, startColor, endColor);
                                    } else {
                                        tv_tv_show_release_date.setTextColor(endColor);
                                        tv_tv_show_vote_average.setTextColor(endColor);
                                    }
                                }
                            }
                        }
                    }))
                    .into(iv_tv_show_poster);
            tv_tv_show_title.setText(subject.title);
            tv_tv_show_release_date.setText(DateUtils.convertDateToStr(subject.release_date, "yyyy"));
            tv_tv_show_vote_average.setText(String.valueOf(subject.vote_average));
        }
    }
}

package com.lcjian.vastplayer.ui.search;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.ui.subject.MovieActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

class SearchedMovieViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.iv_movie_poster)
    ImageView iv_movie_poster;
    @BindView(R.id.tv_movie_title)
    TextView tv_movie_title;
    @BindView(R.id.tv_movie_meta)
    TextView tv_movie_meta;
    @BindView(R.id.tv_movie_director)
    TextView tv_movie_director;
    @BindView(R.id.tv_movie_main_actor)
    TextView tv_movie_main_actor;

    private Subject subject;

    SearchedMovieViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.searched_movie_item, parent, false));
        ButterKnife.bind(this, this.itemView);
        itemView.setOnClickListener(v -> v.getContext().startActivity(
                new Intent(v.getContext(), MovieActivity.class)
                        .putExtra(Constants.BUNDLE_PARAMETER_SUBJECT, subject)));
    }

    void bindTo(Subject subject) {
        this.subject = subject;
        Glide.with(itemView.getContext())
                .load(subject.posters == null || subject.posters.isEmpty() ? "" : subject.posters.get(0).url)
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder_movie).centerCrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_movie_poster);
        tv_movie_title.setText(subject.title);
        tv_movie_director.setText(subject.properties == null ? "" : subject.properties.get("director"));
        tv_movie_main_actor.setText(subject.properties == null ? "" : subject.properties.get("main_actor"));

        StringBuilder metaDataStrBuilder = new StringBuilder(DateUtils.convertDateToStr(subject.release_date, "yyyy"));
        if (subject.properties != null && subject.properties.get("minutes") != null) {
            metaDataStrBuilder.append(" • ");
            metaDataStrBuilder.append(itemView.getContext().getString(R.string.minutes, Integer.parseInt(subject.properties.get("minutes"))));
        }
        tv_movie_meta.setText(metaDataStrBuilder);
    }
}

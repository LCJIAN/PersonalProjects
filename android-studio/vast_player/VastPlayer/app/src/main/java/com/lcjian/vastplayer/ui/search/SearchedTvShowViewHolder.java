package com.lcjian.vastplayer.ui.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.Subject;

import butterknife.BindView;
import butterknife.ButterKnife;

class SearchedTvShowViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.iv_tv_show_poster)
    ImageView iv_tv_show_poster;
    @BindView(R.id.tv_tv_show_title)
    TextView tv_tv_show_title;
    @BindView(R.id.tv_tv_show_meta)
    TextView tv_tv_show_meta;
    @BindView(R.id.tv_tv_show_director)
    TextView tv_tv_show_director;
    @BindView(R.id.tv_tv_show_main_actor)
    TextView tv_tv_show_main_actor;

    private Subject tvShow;

    SearchedTvShowViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.searched_tv_show_item, parent, false));
        ButterKnife.bind(this, this.itemView);
        itemView.setOnClickListener(v -> {
//                v.getContext().startActivity(new Intent(v.getContext(), MovieActivity.class).putExtra(Constants.BUNDLE_PARAMETER_TV_SHOW, tvShow));
        });
    }

    void bindTo(Subject tvShow) {
//        this.tvShow = tvShow;
//        Glide.with(itemView.getContext())
//                .load(tvShow.poster)
//                .apply(RequestOptions.placeholderOf(R.drawable.placeholder_movie).centerCrop())
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(iv_tv_show_poster);
//        tv_tv_show_title.setText(tvShow.title);
//        tv_tv_show_director.setText(tvShow.director);
//        tv_tv_show_main_actor.setText(tvShow.main_actor);
//
//        StringBuilder metaDataStrBuilder = new StringBuilder(DateUtils.convertDateToStr(tvShow.release_date, "yyyy"));
//        if (tvShow.minutes != null && tvShow.minutes != 0) {
//            metaDataStrBuilder.append(" • ");
//            metaDataStrBuilder.append(itemView.getContext().getString(R.string.minutes, tvShow.minutes));
//        }
//        tv_tv_show_meta.setText(metaDataStrBuilder);
    }
}
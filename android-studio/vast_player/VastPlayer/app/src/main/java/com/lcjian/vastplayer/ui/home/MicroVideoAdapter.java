package com.lcjian.vastplayer.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lcjian.lib.media.IMediaPlayer;
import com.lcjian.lib.media.design.VideoView;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.Utils;
import com.lcjian.vastplayer.data.network.RestAPI;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.ui.widget.MediaControllerM;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MicroVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Subject> mData;

    private RestAPI mRestAPI;

    public MicroVideoAdapter(List<Subject> data, RestAPI restAPI) {
        this.mData = data;
        this.mRestAPI = restAPI;
    }

    public void replaceAll(final List<Subject> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {

            @Override
            public int getOldListSize() {
                return mData == null ? 0 : mData.size();
            }

            @Override
            public int getNewListSize() {
                return newData == null ? 0 : newData.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return mData.get(oldItemPosition).id.equals(newData.get(newItemPosition).id);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }
        }, true);
        mData = newData;
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MicroVideoViewHolder(parent, mRestAPI);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MicroVideoViewHolder) {
            ((MicroVideoViewHolder) holder).bindTo(mData.get(position));
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof MicroVideoViewHolder) {
            ((MicroVideoViewHolder) holder).unSubscribe();
        }
    }

    static class MicroVideoViewHolder extends RecyclerView.ViewHolder implements IMediaPlayer.OnInfoListener, View.OnClickListener {

        @BindView(R.id.tv_video_title)
        TextView tv_video_title;
        @BindView(R.id.iv_video_thumbnail)
        ImageView iv_video_thumbnail;
        @BindView(R.id.error_msg)
        TextView error_msg;
        @BindView(R.id.media_controller)
        MediaControllerM media_controller;
        @BindView(R.id.vv_video)
        VideoView vv_video;
        @BindView(R.id.progress_bar)
        ProgressBar progress_bar;

        Subject subject;

        RestAPI restAPI;

        Disposable disposable;

        MicroVideoViewHolder(ViewGroup parent, RestAPI restAPI) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.micro_video_item, parent, false));
            this.restAPI = restAPI;
            ButterKnife.bind(this, this.itemView);
            vv_video.setOnInfoListener(this);
            iv_video_thumbnail.setOnClickListener(this);
            vv_video.setMediaController(media_controller);
        }

        void bindTo(Subject item) {
            this.subject = item;
            reset();
            tv_video_title.setText(subject.title);
            Glide.with(iv_video_thumbnail).load(subject.thumbnails.get(0).url).into(iv_video_thumbnail);
        }

        void reset() {
            iv_video_thumbnail.setVisibility(View.VISIBLE);
            progress_bar.setVisibility(View.GONE);
            media_controller.setVisibility(View.GONE);
            error_msg.setVisibility(View.GONE);
            progress_bar.setProgress(0);
            vv_video.stopPlayback();
            vv_video.setVideoURI(null);
        }

        void unSubscribe() {
            if (disposable != null) {
                disposable.dispose();
            }
        }

        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            switch (what) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    if (vv_video.isPlaying()) {
                        progress_bar.setVisibility(View.VISIBLE);
                    }
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    progress_bar.setVisibility(View.GONE);
                    break;
            }
            return true;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_video_thumbnail:
                    iv_video_thumbnail.setVisibility(View.GONE);
                    progress_bar.setVisibility(View.VISIBLE);
                    media_controller.setVisibility(View.VISIBLE);
                    if (disposable != null) {
                        disposable.dispose();
                    }
                    disposable = restAPI.spunSugarService().subjectSources(subject.id)
                            .flatMap(videoUrls -> Utils.createParseObservable(itemView.getContext(), videoUrls.get(0), restAPI)
                                    .onErrorResumeNext(Observable.mergeDelayError(Arrays.asList(Utils.createSnifferObservable(itemView.getContext(), videoUrls.get(0).url, true),
                                            Utils.createSnifferObservable(itemView.getContext(), videoUrls.get(0).url, false))).firstOrError().toObservable()))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(strings -> {
                                vv_video.setVideoPath(strings.get(0));
                                vv_video.start();
                            }, throwable -> {
                                progress_bar.setVisibility(View.GONE);
                                error_msg.setVisibility(View.VISIBLE);
                            });
                    break;
                default:
                    break;
            }
        }
    }
}

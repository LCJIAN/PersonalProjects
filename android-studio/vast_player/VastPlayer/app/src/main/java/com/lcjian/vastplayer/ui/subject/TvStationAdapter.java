package com.lcjian.vastplayer.ui.subject;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lcjian.vastplayer.Global;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.TvStation;
import com.lcjian.vastplayer.data.network.entity.VideoUrl;
import com.lcjian.vastplayer.ui.player.VideoPlayerActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TvStationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_GROUP = 0;
    private static final int TYPE_TV_STATION = 1;

    private List<Object> mData;

    public TvStationAdapter(List<Object> data) {
        this.mData = data;
    }

    public void replaceAll(final List<Object> data) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {

            @Override
            public int getOldListSize() {
                return mData == null ? 0 : mData.size();
            }

            @Override
            public int getNewListSize() {
                return data == null ? 0 : data.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return mData.get(oldItemPosition) == data.get(newItemPosition);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }
        }, true);
        this.mData = data;
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position) instanceof TvStation ? TYPE_TV_STATION : TYPE_GROUP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == TYPE_GROUP ? new TVStationGroupViewHolder(parent) : new TVStationViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TVStationGroupViewHolder) {
            ((TVStationGroupViewHolder) holder).bindTo((String) mData.get(position));
        } else if (holder instanceof TVStationViewHolder) {
            ((TVStationViewHolder) holder).bindTo((TvStation) mData.get(position));
        }
    }

    static class TVStationGroupViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_tv_station_type_name)
        TextView tv_tv_station_type_name;

        TVStationGroupViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_station_group_item, parent, false));
            ButterKnife.bind(this, this.itemView);
        }

        void bindTo(String typeName) {
            tv_tv_station_type_name.setText(typeName);
        }
    }

    static class TVStationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_tv_station_logo)
        ImageView iv_tv_station_logo;
        @BindView(R.id.tv_tv_station_name)
        TextView tv_tv_station_name;
        @BindView(R.id.tv_tv_station_now)
        TextView tv_tv_station_now;

        TvStation tvStation;

        TVStationViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.tv_station_item, parent, false));
            ButterKnife.bind(this, this.itemView);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                intent.putExtra("title", tvStation.name);
                VideoUrl videoUrl = new VideoUrl();
                videoUrl.url = tvStation.channel;
                videoUrl.type = "tv";
                intent.putExtra("video_url", videoUrl);
                v.getContext().startActivity(intent);
            });
        }

        void bindTo(TvStation tvStation) {
            this.tvStation = tvStation;
            Glide.with(itemView.getContext())
                    .load(tvStation.logo)
                    .apply(Global.moviePoster)
                    .transition(Global.crossFade)
                    .into(iv_tv_station_logo);
            tv_tv_station_name.setText(tvStation.name);
            if (tvStation.now != null && !TextUtils.isEmpty(tvStation.now.time)) {
                tv_tv_station_now.setText(String.format("%s     %s", tvStation.now.time, tvStation.now.name));
            } else {
                tv_tv_station_now.setText("");
            }
        }
    }
}

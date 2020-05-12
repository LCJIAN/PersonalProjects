package com.lcjian.vastplayer.ui.search;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.vastplayer.data.network.entity.Subject;

import java.util.List;

class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MOVIE = 0;
    private static final int TYPE_TV_SHOW = 1;

    private List<Object> mData;

    SearchResultAdapter(List<Object> data) {
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
        return mData.get(position) instanceof Subject ? TYPE_MOVIE : TYPE_TV_SHOW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == TYPE_MOVIE ? new SearchedMovieViewHolder(parent) : new SearchedTvShowViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SearchedMovieViewHolder) {
            ((SearchedMovieViewHolder) holder).bindTo((Subject) mData.get(position));
        } else if (holder instanceof SearchedTvShowViewHolder) {
            ((SearchedTvShowViewHolder) holder).bindTo((Subject) mData.get(position));
        }
    }
}

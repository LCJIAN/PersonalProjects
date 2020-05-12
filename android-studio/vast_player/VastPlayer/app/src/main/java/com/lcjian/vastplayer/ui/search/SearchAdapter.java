package com.lcjian.vastplayer.ui.search;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.RxBus;
import com.lcjian.vastplayer.data.db.entity.SearchHistory;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<SearchHistory> mSearchHistories;

    private RxBus mBus;

    public SearchAdapter(RxBus bus) {
        this.mBus = bus;
        this.mSearchHistories = new ArrayList<>();
    }

    public void replaceAll(final List<SearchHistory> searchHistories) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {

            @Override
            public int getOldListSize() {
                return mSearchHistories == null ? 0 : mSearchHistories.size();
            }

            @Override
            public int getNewListSize() {
                return searchHistories == null ? 0 : searchHistories.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return TextUtils.equals(mSearchHistories.get(oldItemPosition).text, searchHistories.get(newItemPosition).text);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }
        }, true);
        mSearchHistories = searchHistories;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final SearchViewHolder holder = new SearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false));
        View.OnClickListener onClickListener = v -> mBus.send(holder.searchHistory);
        holder.itemView.setOnClickListener(onClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        holder.searchHistory = mSearchHistories.get(position);
        holder.tv_search.setText(holder.searchHistory.text);
    }

    @Override
    public int getItemCount() {
        return mSearchHistories == null ? 0 : mSearchHistories.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_search)
        TextView tv_search;

        SearchHistory searchHistory;

        SearchViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}

package com.lcjian.cloudlocation.ui.base;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class EmptyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RecyclerView.Adapter mInnerAdapter;

    private View mEmptyView;

    private int mEmptyViewType;
    private boolean mPreEmpty = true;
    private RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
            mPreEmpty = mInnerAdapter.getItemCount() == 0;
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            boolean empty = mInnerAdapter.getItemCount() == 0;
            if (mPreEmpty || empty) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeChanged(positionStart, itemCount);
            }
            mPreEmpty = empty;
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            boolean empty = mInnerAdapter.getItemCount() == 0;
            if (mPreEmpty || empty) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeChanged(positionStart, itemCount, payload);
            }
            mPreEmpty = empty;
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            boolean empty = mInnerAdapter.getItemCount() == 0;
            if (mPreEmpty || empty) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeInserted(positionStart, itemCount);
            }
            mPreEmpty = empty;
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            boolean empty = mInnerAdapter.getItemCount() == 0;
            if (mPreEmpty || empty) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeRemoved(positionStart, itemCount);
            }
            mPreEmpty = empty;
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            boolean empty = mInnerAdapter.getItemCount() == 0;
            if (mPreEmpty || empty) {
                notifyDataSetChanged();
            } else {
                notifyItemRangeChanged(fromPosition, toPosition + itemCount);
            }
            mPreEmpty = empty;
        }
    };

    public EmptyAdapter(RecyclerView.Adapter adapter) {
        setInnerAdapter(adapter);
    }

    public EmptyAdapter setEmptyView(View emptyView) {
        mEmptyView = emptyView;
        mEmptyViewType = mEmptyView.hashCode();
        return this;
    }

    public void showEmptyView() {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    public void hideEmptyView() {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.INVISIBLE);
        }
    }

    public void setInnerAdapter(RecyclerView.Adapter adapter) {
        if (mInnerAdapter != null) {
            mInnerAdapter.unregisterAdapterDataObserver(dataObserver);
        }

        this.mInnerAdapter = adapter;
        mInnerAdapter.registerAdapterDataObserver(dataObserver);
    }

    @Override
    public int getItemCount() {
        if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
            return 1;
        } else {
            return mInnerAdapter.getItemCount();
        }
    }

    @Override
    public long getItemId(int position) {
        if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
            return mEmptyViewType;
        }
        return mInnerAdapter.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
            return mEmptyViewType;
        }
        return mInnerAdapter.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
            return new Holder(mEmptyView);
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
            return;
        }
        mInnerAdapter.onBindViewHolder(holder, position);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
            return;
        }
        mInnerAdapter.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mInnerAdapter.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
                        return gridLayoutManager.getSpanCount();
                    }
                    if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
            gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams != null && layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                p.setFullSpan(true);
            }
        } else {
            mInnerAdapter.onViewAttachedToWindow(holder);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
            return super.onFailedToRecycleView(holder);
        } else {
            return mInnerAdapter.onFailedToRecycleView(holder);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mInnerAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
            super.onViewDetachedFromWindow(holder);
        } else {
            mInnerAdapter.onViewDetachedFromWindow(holder);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (mInnerAdapter.getItemCount() == 0 && mEmptyView != null) {
            super.onViewRecycled(holder);
        } else {
            mInnerAdapter.onViewRecycled(holder);
        }
    }

    private static class Holder extends RecyclerView.ViewHolder {

        private Holder(View itemView) {
            super(itemView);
        }
    }
}

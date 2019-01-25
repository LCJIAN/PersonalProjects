package com.lcjian.mmt.ui.base;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class AdvanceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RecyclerView.Adapter mInnerAdapter;
    private OnItemClickListener mOnItemClickListener;
    private List<Entry> headers, footers;
    private RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart + headers.size(), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            notifyItemRangeChanged(positionStart + headers.size(), itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart + headers.size(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart + headers.size(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            int headCount = headers.size();
            notifyItemRangeChanged(fromPosition + headCount, toPosition + headCount + itemCount);
        }
    };

    public AdvanceAdapter(RecyclerView.Adapter adapter) {
        headers = new ArrayList<>();
        footers = new ArrayList<>();
        setInnerAdapter(adapter);
    }

    public void setInnerAdapter(RecyclerView.Adapter adapter) {
        if (mInnerAdapter != null) {
            notifyItemRangeRemoved(headers.size(), mInnerAdapter.getItemCount());
            mInnerAdapter.unregisterAdapterDataObserver(dataObserver);
        }

        this.mInnerAdapter = adapter;
        mInnerAdapter.registerAdapterDataObserver(dataObserver);
        notifyItemRangeInserted(headers.size(), mInnerAdapter.getItemCount());
    }

    public AdvanceAdapter setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
        return this;
    }

    @Override
    public int getItemCount() {
        return headers.size() + footers.size() + mInnerAdapter.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        int innerCount = mInnerAdapter.getItemCount();
        int headCount = headers.size();
        if (position < headCount) {
            return headers.get(position).viewType;
        } else if (headCount <= position && position < headCount + innerCount) {
            return mInnerAdapter.getItemId(position - headCount);
        } else {
            return footers.get(position - headCount - innerCount).viewType;
        }
    }

    @Override
    public int getItemViewType(int position) {
        int innerCount = mInnerAdapter.getItemCount();
        int headCount = headers.size();
        if (position < headCount) {
            return headers.get(position).viewType;
        } else if (headCount <= position && position < headCount + innerCount) {
            return mInnerAdapter.getItemViewType(position - headCount);
        } else {
            return footers.get(position - headCount - innerCount).viewType;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        for (Entry header : headers) {
            if (header.viewType == viewType) {
                return new Holder(header.view);
            }
        }
        for (Entry footer : footers) {
            if (footer.viewType == viewType) {
                return new Holder(footer.view);
            }
        }
        RecyclerView.ViewHolder holder = mInnerAdapter.onCreateViewHolder(parent, viewType);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    mOnItemClickListener.onItemClick(itemView);
                }
            });
        }
        return holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int headCount = headers.size();
        if (headCount <= position && position < headCount + mInnerAdapter.getItemCount()) {
            mInnerAdapter.onBindViewHolder(holder, position - headCount);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        int headCount = headers.size();
        if (headCount <= position && position < headCount + mInnerAdapter.getItemCount()) {
            mInnerAdapter.onBindViewHolder(holder, position - headCount, payloads);
        }
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
                    if (isSingle(position)) {
                        return gridLayoutManager.getSpanCount();
                    } else {
                        if (spanSizeLookup != null) {
                            return spanSizeLookup.getSpanSize(position);
                        }
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
        int position = holder.getAdapterPosition();
        if (isHeader(position) || isFooter(position)) {
            if (isSingle(position)) {
                ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();

                if (layoutParams != null && layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                    StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                    p.setFullSpan(true);
                }
            }
        } else {
            mInnerAdapter.onViewAttachedToWindow(holder);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        int position = holder.getAdapterPosition();
        if (isHeader(position) || isFooter(position)) {
            super.onViewRecycled(holder);
        } else {
            mInnerAdapter.onViewRecycled(holder);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        int position = holder.getAdapterPosition();
        if (isHeader(position) || isFooter(position)) {
            super.onViewDetachedFromWindow(holder);
        } else {
            mInnerAdapter.onViewDetachedFromWindow(holder);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mInnerAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        int position = holder.getAdapterPosition();
        if (isHeader(position) || isFooter(position)) {
            return super.onFailedToRecycleView(holder);
        } else {
            return mInnerAdapter.onFailedToRecycleView(holder);
        }
    }

    public void addHeader(View view) {
        addHeader(view, true);
    }

    public void addHeader(View view, boolean singleLine) {
        addHeader(headers.size(), view, singleLine);
    }

    public void addHeader(int location, View view, boolean singleLine) {

        for (Entry header : headers) {
            if (header.view == view) {
                return;
            }
        }
        // FIXME may conflict with this inner adapter
        headers.add(location, new Entry(view, view.hashCode(), singleLine));
        notifyItemInserted(location);
    }

    public void removeHeader(View view) {
        if (view == null) {
            return;
        }
        int i = 0;
        for (Entry header : headers) {
            if (header.view == view) {
                headers.remove(header);
                notifyItemRemoved(i);
                break;
            }
            i++;
        }
    }

    public void addFooter(View view) {
        addFooter(view, true);
    }

    public void addFooter(View view, boolean singleLine) {
        addFooter(footers.size(), view, singleLine);
    }

    public void addFooter(int location, View view, boolean singleLine) {
        if (view == null) {
            return;
        }
        for (Entry footer : footers) {
            if (footer.view == view) {
                return;
            }
        }
        // FIXME may conflict with this inner adapter
        footers.add(location, new Entry(view, view.hashCode(), singleLine));
        notifyItemInserted(headers.size() + mInnerAdapter.getItemCount() + location);
    }

    public void removeFooter(View view) {
        if (view == null) {
            return;
        }
        int i = 0;
        for (Entry footer : footers) {
            if (footer.view == view) {
                footers.remove(footer);
                notifyItemRemoved(headers.size() + mInnerAdapter.getItemCount() + i);
                break;
            }
            i++;
        }
    }

    public boolean isHeader(int position) {
        return position < headers.size();
    }

    public boolean isFooter(int position) {
        return headers.size() + mInnerAdapter.getItemCount() <= position;
    }

    public boolean isSingle(int position) {
        return (isHeader(position) && headers.get(position).singleLine)
                || (isFooter(position) && footers.get(position - headers.size() - mInnerAdapter.getItemCount()).singleLine);
    }

    public interface OnItemClickListener {

        void onItemClick(View itemView);
    }

    private static class Holder extends RecyclerView.ViewHolder {

        private Holder(View itemView) {
            super(itemView);
        }
    }

    private static class Entry {
        View view;
        int viewType;
        boolean singleLine;

        private Entry(View view, int viewType, boolean singleLine) {
            this.view = view;
            this.viewType = viewType;
            this.singleLine = singleLine;
        }
    }
}

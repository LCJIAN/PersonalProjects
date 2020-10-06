package com.lcjian.lib.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LoopAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RecyclerView mParent;

    private RecyclerView.Adapter<? super RecyclerView.ViewHolder> mInnerAdapter;

    private Runnable mLooper;

    public LoopAdapter(RecyclerView.Adapter<? super RecyclerView.ViewHolder> innerAdapter) {
        this.mInnerAdapter = innerAdapter;
        this.mLooper = new Runnable() {
            @Override
            public void run() {
                if (mParent != null) {
                    mParent.scrollBy(0, 1);
                    ViewCompat.postOnAnimation(mParent, this);
                }
            }
        };
    }

    public void start() {
        if (mParent != null) {
            mParent.removeCallbacks(mLooper);
            mParent.postDelayed(mLooper, 1000);
        }
    }

    public void stop() {
        if (mParent != null) {
            mParent.removeCallbacks(mLooper);
        }
    }

    @Override
    public int getItemCount() {
        return mInnerAdapter.getItemCount() == 0 ? 0 : Integer.MAX_VALUE;
    }

    @Override
    public int getItemViewType(int position) {
        return mInnerAdapter.getItemViewType(position % mInnerAdapter.getItemCount());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        mInnerAdapter.onBindViewHolder(holder, position % mInnerAdapter.getItemCount());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        mInnerAdapter.onBindViewHolder(holder, position % mInnerAdapter.getItemCount(), payloads);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mParent = recyclerView;
        start();
        mInnerAdapter.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mParent = null;
        stop();
        mInnerAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        mInnerAdapter.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        mInnerAdapter.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        mInnerAdapter.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        return mInnerAdapter.onFailedToRecycleView(holder);
    }
}

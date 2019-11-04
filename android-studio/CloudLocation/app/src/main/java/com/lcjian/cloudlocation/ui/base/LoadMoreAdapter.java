package com.lcjian.cloudlocation.ui.base;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class LoadMoreAdapter extends AdvanceAdapter {

    public static final int STATE_DEFAULT = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_ERROR = 2;
    public static final int STATE_END = 3;

    private boolean mEnabled = true;

    private View mLoadingView;

    private View mErrorView;

    private View mEndView;

    private int mState = STATE_DEFAULT;

    private OnLoadMoreListener mOnLoadMoreListener;

    private EndlessRecyclerOnScrollListener mOnScrollListener;

    private Handler mHandler;

    private Runnable mStateSetter;

    public LoadMoreAdapter(RecyclerView.Adapter adapter) {
        super(adapter);
        this.mOnScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                if (mOnLoadMoreListener != null) {
                    mOnLoadMoreListener.onLoadMore();
                }
            }
        };
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mStateSetter = new Runnable() {
            @Override
            public void run() {
                switch (mState) {
                    case STATE_DEFAULT:
                        removeFooter(mLoadingView);
                        removeFooter(mErrorView);
                        removeFooter(mEndView);
                        break;
                    case STATE_LOADING:
                        removeFooter(mErrorView);
                        removeFooter(mEndView);
                        addFooter(mLoadingView);
                        break;
                    case STATE_ERROR:
                        removeFooter(mLoadingView);
                        removeFooter(mEndView);
                        addFooter(mErrorView);
                        break;
                    case STATE_END:
                        removeFooter(mLoadingView);
                        removeFooter(mErrorView);
                        addFooter(mEndView);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(mOnScrollListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnScrollListener(mOnScrollListener);
    }

    public LoadMoreAdapter setLoadingView(View loadingView) {
        if (mLoadingView != null) {
            removeFooter(mLoadingView);
        }
        mLoadingView = loadingView;
        return this;
    }

    public LoadMoreAdapter setErrorView(View errorView) {
        if (mErrorView != null) {
            removeFooter(mErrorView);
        }
        mErrorView = errorView;
        return this;
    }

    public LoadMoreAdapter setEndView(View endView) {
        if (mEndView != null) {
            removeFooter(mEndView);
        }
        mEndView = endView;
        return this;
    }

    public LoadMoreAdapter setEnabled(boolean enabled) {
        mEnabled = enabled;
        return this;
    }

    public LoadMoreAdapter setThreshold(int visibleThreshold) {
        mOnScrollListener.setThreshold(visibleThreshold);
        return this;
    }

    public LoadMoreAdapter setOnLoadMoreListener(OnLoadMoreListener onLoadmoreListener) {
        this.mOnLoadMoreListener = onLoadmoreListener;
        return this;
    }

    public void setState(int state) {
        mState = state;
        mHandler.post(mStateSetter);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    /**
     * Custom Scroll listener for RecyclerView.
     * Based on implementation https://gist.github.com/ssinss/e06f12ef66c51252563e
     */
    private abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

        private int mVisibleThreshold = 5; // The minimum amount of items to have below your current scroll position before mLoading more.

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerViewPositionHelper recyclerViewHelper = RecyclerViewPositionHelper.createHelper(recyclerView);
            int visibleItemCount = recyclerView.getChildCount();
            int totalItemCount = recyclerViewHelper.getItemCount();
            int firstVisibleItem = recyclerViewHelper.findFirstVisibleItemPosition();

            if (mEnabled && mState == STATE_DEFAULT && (totalItemCount - visibleItemCount) <= (firstVisibleItem + mVisibleThreshold)) {
                // End has been reached
                // Do something
                onLoadMore();
            }
        }

        private void setThreshold(int visibleThreshold) {
            this.mVisibleThreshold = visibleThreshold;
        }

        public abstract void onLoadMore();
    }
}

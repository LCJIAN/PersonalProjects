package com.lcjian.vastplayer.ui.mine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.db.AppDatabase;
import com.lcjian.vastplayer.data.db.entity.WatchHistory;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.ui.subject.MovieActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;

public class WatchHistorySubjectAdapter extends RecyclerView.Adapter<WatchHistorySubjectAdapter.MovieViewHolder> {

    private List<Subject> mData;

    private AppDatabase mAppDatabase;

    private boolean mCanceled;

    private ActionMode mActionMode;

    private ActionMode.Callback mCallback;

    private boolean mInActionMode;

    private List<Subject> mChecked;

    private ViewGroup mParent;

    public WatchHistorySubjectAdapter(List<Subject> data, AppDatabase storIOSQLite) {
        this.mData = data;
        this.mAppDatabase = storIOSQLite;
        this.mChecked = new ArrayList<>();
        this.mCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mActionMode = mode;
                mActionMode.setTitle(R.string.action_movie);
                mActionMode.setSubtitle(mParent.getContext().getString(R.string.items_count_selected, mChecked.size()));
                mode.getMenuInflater().inflate(R.menu.menu_delete, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                mInActionMode = true;
                TransitionManager.beginDelayedTransition(mParent);
                notifyItemRangeChanged(0, getItemCount(), mInActionMode);
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete) {
                    deleteHistories(mChecked);
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mInActionMode = false;
                mChecked.clear();
                TransitionManager.beginDelayedTransition(mParent);
                notifyItemRangeChanged(0, getItemCount(), mInActionMode);
            }
        };
    }

    public void replaceAll(final List<Subject> data) {
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
                return mData.get(oldItemPosition).id.equals(data.get(newItemPosition).id);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return mData.get(oldItemPosition).watchHistory.updateTime.equals(data.get(newItemPosition).watchHistory.updateTime);
            }
        }, true);
        mData = data;
        mChecked.clear();
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public WatchHistorySubjectAdapter.MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mParent == null) {
            mParent = parent;
        }
        final WatchHistorySubjectAdapter.MovieViewHolder holder = new WatchHistorySubjectAdapter.MovieViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_history_item, parent, false));
        holder.chb_history.setOnClickListener(v -> checkHistory(holder));
        holder.itemView.setOnClickListener(v -> {
            if (mInActionMode) {
                checkHistory(holder);
            } else {
                v.getContext().startActivity(new Intent(v.getContext(), MovieActivity.class)
                        .putExtra(Constants.BUNDLE_PARAMETER_SUBJECT, holder.subject));
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            ((AppCompatActivity) v.getContext()).startSupportActionMode(mCallback);
            checkHistory(holder);
            return true;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final WatchHistorySubjectAdapter.MovieViewHolder holder, int position) {
        Subject subject = mData.get(position);
        holder.subject = subject;
        Context context = holder.itemView.getContext();
        Glide.with(context)
                .load(subject.posters == null ? null : subject.posters.get(0).url)
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder_movie).centerCrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.iv_movie_poster);
        holder.tv_movie_title.setText(subject.title);
        holder.tv_watch_video_name.setText(subject.watchHistory.subjectVideoName);
        holder.pb_watch_time.setProgress((int) (subject.watchHistory.watchTime
                / ((float) subject.watchHistory.duration) * holder.pb_watch_time.getMax()));
        holder.tv_update_time.setText(DateUtils.convertDateToStr(subject.watchHistory.updateTime, DateUtils.YYYY_MM_DD_HH_MM_SS));
        holder.chb_history.setVisibility(mInActionMode ? View.VISIBLE : View.GONE);
        holder.chb_history.setChecked(mChecked.contains(subject));
        holder.itemView.setActivated(mChecked.contains(subject) && mInActionMode);
        holder.itemView.setLongClickable(!mInActionMode);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchHistorySubjectAdapter.MovieViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            holder.chb_history.setVisibility(mInActionMode ? View.VISIBLE : View.GONE);
            holder.chb_history.setChecked(mChecked.contains(holder.subject));
            holder.itemView.setActivated(mChecked.contains(holder.subject) && mInActionMode);
            holder.itemView.setLongClickable(!mInActionMode);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    private void checkHistory(WatchHistorySubjectAdapter.MovieViewHolder holder) {
        if (mChecked.contains(holder.subject)) {
            mChecked.remove(holder.subject);
        } else {
            mChecked.add(holder.subject);
        }
        if (mActionMode != null) {
            mActionMode.setSubtitle(holder.itemView.getContext().getString(R.string.items_count_selected, mChecked.size()));
        }
        notifyItemChanged(holder.getAdapterPosition(), mInActionMode);
    }

    private void deleteHistories(List<Subject> histories) {
        final List<Subject> deleteHistories = new ArrayList<>(histories);
        final List<Subject> newHistories = new ArrayList<>(mData);
        final List<Subject> oldHistories = new ArrayList<>(mData);
        newHistories.removeAll(deleteHistories);
        replaceAll(newHistories);

        mCanceled = false;
        Snackbar.make(((Activity) mParent.getContext()).findViewById(R.id.cl_watch_history),
                R.string.delete_item, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    mCanceled = true;
                    replaceAll(oldHistories);
                })
                .addCallback(new Snackbar.Callback() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (!mCanceled) {
                            Observable
                                    .fromIterable(deleteHistories)
                                    .map(subject -> subject.watchHistory)
                                    .toList()
                                    .subscribe(watchHistories -> {
                                        WatchHistory[] w = new WatchHistory[watchHistories.size()];
                                        mAppDatabase
                                                .watchHistoryDao()
                                                .delete(watchHistories.toArray(w));
                                    });
                        }
                    }
                }).show();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.chb_history)
        CheckBox chb_history;
        @BindView(R.id.iv_movie_poster)
        ImageView iv_movie_poster;
        @BindView(R.id.tv_movie_title)
        TextView tv_movie_title;
        @BindView(R.id.tv_watch_video_name)
        TextView tv_watch_video_name;
        @BindView(R.id.pb_watch_time)
        ProgressBar pb_watch_time;
        @BindView(R.id.tv_update_time)
        TextView tv_update_time;

        Subject subject;

        MovieViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
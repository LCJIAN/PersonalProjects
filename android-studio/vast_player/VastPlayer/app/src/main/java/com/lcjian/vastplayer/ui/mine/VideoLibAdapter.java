package com.lcjian.vastplayer.ui.mine;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.lib.util.common.FileUtils;
import com.lcjian.lib.util.common.StringUtils;
import com.lcjian.lib.widget.RatioLayout;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.db.AppDatabase;
import com.lcjian.vastplayer.data.db.entity.VideoLocal;
import com.lcjian.vastplayer.data.entity.VideoGroup;
import com.lcjian.vastplayer.data.network.entity.VideoUrl;
import com.lcjian.vastplayer.ui.player.VideoPlayerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoLibAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_VIDEO_LIST = 0;
    private static final int TYPE_VIDEO_GRID = 1;
    private static final int TYPE_VIDEO_GROUP = 2;

    private List<Object> mData;

    private AppDatabase mAppDatabase;

    private GridLayoutManager mGridLayoutManager;

    private boolean mCanceled;

    private ActionMode mActionMode;

    private ActionMode.Callback mCallback;

    private boolean mInActionMode;

    private List<VideoLocal> mChecked;

    private ViewGroup mParent;

    VideoLibAdapter(List<Object> data, AppDatabase storIOSQLite, GridLayoutManager gridLayoutManager) {
        super();
        this.mData = data;
        this.mAppDatabase = storIOSQLite;
        this.mGridLayoutManager = gridLayoutManager;
        this.mChecked = new ArrayList<>();
        this.mCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mActionMode = mode;
                mActionMode.setTitle(R.string.action_video_library);
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
                    deleteVideos(mChecked);
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
                Object oldItem = mData.get(oldItemPosition);
                Object newItem = data.get(newItemPosition);
                return (oldItem instanceof VideoLocal
                        && newItem instanceof VideoLocal
                        && TextUtils.equals(((VideoLocal) oldItem).title, ((VideoLocal) newItem).title))
                        || (oldItem instanceof VideoGroup
                        && newItem instanceof VideoGroup
                        && TextUtils.equals(((VideoGroup) oldItem).name, ((VideoGroup) newItem).name));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }
        }, true);
        mData = data;
        mChecked.clear();
        diffResult.dispatchUpdatesTo(this);
    }

    public List<Object> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public long getItemId(int position) {
        Object displayable = mData.get(position);
        if (displayable instanceof VideoLocal) {
            Long id = ((VideoLocal) displayable).id;
            if (id == null) {
                return displayable.hashCode();
            } else {
                return id;
            }
        } else if (displayable instanceof VideoGroup) {
            return ((VideoGroup) displayable).name.hashCode();
        }
        return displayable.hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position) instanceof VideoLocal
                ? (mGridLayoutManager.getSpanCount() == 1 ? TYPE_VIDEO_LIST : TYPE_VIDEO_GRID)
                : TYPE_VIDEO_GROUP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == TYPE_VIDEO_LIST ? new VideoLibViewHolder(parent, this)
                : (viewType == TYPE_VIDEO_GRID ? new VideoLibGridViewHolder(parent, this) : new VideoGroupViewHolder(parent, this));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoLibViewHolder) {
            ((VideoLibViewHolder) holder).bindTo((VideoLocal) mData.get(position));
        } else if (holder instanceof VideoLibGridViewHolder) {
            ((VideoLibGridViewHolder) holder).bindTo((VideoLocal) mData.get(position));
        } else if (holder instanceof VideoGroupViewHolder) {
            ((VideoGroupViewHolder) holder).bindTo((VideoGroup) mData.get(position));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            if (holder instanceof VideoLibViewHolder) {
                ((VideoLibViewHolder) holder).updateActionMode();
            } else if (holder instanceof VideoLibGridViewHolder) {
                ((VideoLibGridViewHolder) holder).updateActionMode();
            } else if (holder instanceof VideoGroupViewHolder) {
                ((VideoGroupViewHolder) holder).updateActionMode();
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mParent = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mParent = null;
    }

    public static void renameFile(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        //执行重命名
        oleFile.renameTo(newFile);
    }

    static class VideoLibViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.chb_video)
        CheckBox chb_video;
        @BindView(R.id.iv_video_thumbnail)
        ImageView iv_video_thumbnail;
        @BindView(R.id.tv_video_name)
        TextView tv_video_name;
        @BindView(R.id.tv_video_duration)
        TextView tv_video_duration;
        @BindView(R.id.tv_video_size)
        TextView tv_video_size;
        @BindView(R.id.tv_video_resolution)
        TextView tv_video_resolution;

        private VideoLocal video;

        private VideoLibAdapter mAdapter;

        VideoLibViewHolder(ViewGroup parent, VideoLibAdapter adapter) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false));
            this.mAdapter = adapter;
            ButterKnife.bind(this, this.itemView);
            chb_video.setOnClickListener(v -> checkVideo());

            itemView.setOnClickListener(v -> {
                if (mAdapter.mInActionMode) {
                    checkVideo();
                } else {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                    intent.putExtra("title", video.title);
                    VideoUrl videoUrl = new VideoUrl();
                    videoUrl.url = video.data;
                    videoUrl.type = "direct";
                    intent.putExtra("video_url", videoUrl);
                    v.getContext().startActivity(intent);
                }
            });
            itemView.setOnLongClickListener(v -> {
                ((AppCompatActivity) v.getContext()).startSupportActionMode(mAdapter.mCallback);
                checkVideo();
                return true;
            });
        }

        void bindTo(VideoLocal video) {
            this.video = video;
            Glide.with(itemView.getContext())
                    .load(video.data)
                    .thumbnail(1)
                    .apply(RequestOptions.overrideOf(video.width.intValue(), video.height.intValue())
                            .placeholder(R.drawable.color_video_place_holder).centerCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv_video_thumbnail);
            tv_video_name.setText(video.title);
            tv_video_duration.setText(StringUtils.stringForTime(video.duration.intValue()));
            tv_video_size.setText(Formatter.formatFileSize(tv_video_size.getContext(), video.size));
            tv_video_resolution.setText(String.format(Locale.getDefault(), "%dX%d", video.width, video.height));
            updateActionMode();
        }

        void updateActionMode() {
            chb_video.setVisibility(mAdapter.mInActionMode ? View.VISIBLE : View.GONE);
            chb_video.setChecked(mAdapter.mChecked.contains(video));
            itemView.setActivated(mAdapter.mChecked.contains(video) && mAdapter.mInActionMode);
            itemView.setLongClickable(!mAdapter.mInActionMode);
        }

        private void checkVideo() {
            if (mAdapter.mChecked.contains(video)) {
                mAdapter.mChecked.remove(video);
            } else {
                mAdapter.mChecked.add(video);
            }
            if (mAdapter.mActionMode != null) {
                mAdapter.mActionMode.setSubtitle(itemView.getContext().getString(R.string.items_count_selected, mAdapter.mChecked.size()));
            }
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            int i = position - 1;
            while (true) {
                if (mAdapter.mData.get(i) instanceof VideoGroup) {
                    break;
                }
                i--;
            }
            mAdapter.notifyItemChanged(getAdapterPosition(), mAdapter.mInActionMode);
            mAdapter.notifyItemChanged(i, mAdapter.mInActionMode);
        }
    }

    static class VideoLibGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.chb_video)
        CheckBox chb_video;
        @BindView(R.id.rl_video)
        RatioLayout rl_video;
        @BindView(R.id.iv_video_thumbnail)
        ImageView iv_video_thumbnail;
        @BindView(R.id.tv_video_name)
        TextView tv_video_name;
        @BindView(R.id.tv_video_duration)
        TextView tv_video_duration;

        private VideoLocal video;

        private VideoLibAdapter mAdapter;

        VideoLibGridViewHolder(ViewGroup parent, VideoLibAdapter adapter) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_grid_item, parent, false));
            this.mAdapter = adapter;
            ButterKnife.bind(this, this.itemView);
            chb_video.setOnClickListener(v -> checkVideo());

            itemView.setOnClickListener(v -> {
                if (mAdapter.mInActionMode) {
                    checkVideo();
                } else {
                    int position = getAdapterPosition();
                    if (position == -1) {
                        return;
                    }
                    Intent intent = new Intent(v.getContext(), VideoPlayerActivity.class);
                    intent.putExtra("title", video.title);
                    VideoUrl videoUrl = new VideoUrl();
                    videoUrl.url = video.data;
                    videoUrl.type = "direct";
                    intent.putExtra("video_url", videoUrl);
                    v.getContext().startActivity(intent);
                }
            });
            itemView.setOnLongClickListener(v -> {
                ((AppCompatActivity) v.getContext()).startSupportActionMode(mAdapter.mCallback);
                checkVideo();
                return true;
            });
        }

        void bindTo(VideoLocal video) {
            this.video = video;
            Glide.with(itemView.getContext())
                    .load(video.data)
                    .thumbnail(1)
                    .apply(RequestOptions.overrideOf(video.width.intValue(), video.height.intValue())
                            .placeholder(R.drawable.color_video_place_holder).centerCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv_video_thumbnail);
            tv_video_name.setText(video.title);
            if (mAdapter.mGridLayoutManager.getSpanSizeLookup().getSpanSize(mAdapter.mData.indexOf(video)) == 2) {
                tv_video_name.setVisibility(View.GONE);
                rl_video.setRatio(1);
            } else {
                tv_video_name.setVisibility(View.VISIBLE);
                rl_video.setRatio(0.5625f);
            }
            tv_video_duration.setText(StringUtils.stringForTime(video.duration.intValue()));

            updateActionMode();
        }

        void updateActionMode() {
            int margin = 0;
            if (mAdapter.mInActionMode && mAdapter.mChecked.contains(video)) {
                margin = (int) DimenUtils.dipToPixels(12, itemView.getContext());
            }
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) iv_video_thumbnail.getLayoutParams();
            layoutParams.setMargins(margin, margin, margin, margin);
            iv_video_thumbnail.setLayoutParams(layoutParams);
            tv_video_name.setVisibility(mAdapter.mGridLayoutManager.getSpanSizeLookup().getSpanSize(mAdapter.mData.indexOf(video)) == 2
                    || mAdapter.mInActionMode ? View.GONE : View.VISIBLE);
            tv_video_duration.setVisibility(mAdapter.mInActionMode ? View.GONE : View.VISIBLE);
            chb_video.setVisibility(mAdapter.mInActionMode ? View.VISIBLE : View.GONE);
            chb_video.setChecked(mAdapter.mChecked.contains(video));
            itemView.setActivated(mAdapter.mChecked.contains(video) && mAdapter.mInActionMode);
            itemView.setLongClickable(!mAdapter.mInActionMode);
        }

        private void checkVideo() {
            if (mAdapter.mChecked.contains(video)) {
                mAdapter.mChecked.remove(video);
            } else {
                mAdapter.mChecked.add(video);
            }
            if (mAdapter.mActionMode != null) {
                mAdapter.mActionMode.setSubtitle(itemView.getContext().getString(R.string.items_count_selected, mAdapter.mChecked.size()));
            }
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            int i = position - 1;
            while (true) {
                if (mAdapter.mData.get(i) instanceof VideoGroup) {
                    break;
                }
                i--;
            }
            mAdapter.notifyItemChanged(i, mAdapter.mInActionMode);
            TransitionManager.beginDelayedTransition(mAdapter.mParent);
            mAdapter.notifyItemChanged(getAdapterPosition(), mAdapter.mInActionMode);
        }
    }

    private void deleteVideos(List<VideoLocal> videos) {
        final List<VideoLocal> deleteVideos = new ArrayList<>(videos);
        final List<VideoGroup> deleteVideoGroups = new ArrayList<>();
        final List<Object> newObject = new ArrayList<>(mData);
        final List<Object> oldObject = new ArrayList<>(mData);
        for (Object item : newObject) {
            if (item instanceof VideoGroup) {
                if (deleteVideos.containsAll(((VideoGroup) item).localVideos)) {
                    deleteVideoGroups.add((VideoGroup) item);
                }
            }
        }
        newObject.removeAll(deleteVideos);
        newObject.removeAll(deleteVideoGroups);
        replaceAll(newObject);

        mCanceled = false;
        Snackbar.make(((Activity) mParent.getContext()).findViewById(R.id.cl_video_lib), R.string.delete_item, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    mCanceled = true;
                    replaceAll(oldObject);
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (!mCanceled) {
                            for (VideoLocal video : deleteVideos) {
                                FileUtils.deleteFile(video.data);
//                                VideoLibAdapter.renameFile(video.data, Environment.getExternalStoragePublicDirectory("Download").getPath() + "/" + FileUtils.getFileName(video.data));
                            }
                            for (Object item : newObject) {
                                if (item instanceof VideoGroup) {
                                    ((VideoGroup) item).localVideos.removeAll(deleteVideos);
                                }
                            }
                            VideoLocal[] v = new VideoLocal[deleteVideos.size()];
                            mAppDatabase
                                    .videoLocalDao()
                                    .delete(deleteVideos.toArray(v));
                            notifyItemRangeChanged(0, getItemCount());
                        }
                    }
                }).show();
    }

    static class VideoGroupViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.chb_video_group)
        CheckBox chb_video_group;
        @BindView(R.id.tv_video_group_name)
        TextView tv_video_group_name;

        private VideoGroup videoGroup;

        private VideoLibAdapter mAdapter;

        VideoGroupViewHolder(ViewGroup parent, VideoLibAdapter adapter) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_group_title_item, parent, false));
            this.mAdapter = adapter;
            ButterKnife.bind(this, this.itemView);

            chb_video_group.setOnClickListener(v -> checkGroupVideos());
            tv_video_group_name.setOnClickListener(v -> {
                if (videoGroup.name.contains("/")) {
                    Intent intent = Intent.createChooser(
                            new Intent()
                                    .setAction(Intent.ACTION_GET_CONTENT)
                                    .addCategory(Intent.CATEGORY_OPENABLE)
                                    .setDataAndType(Uri.fromFile(new File(videoGroup.localVideos.get(0).data).getParentFile()), "*/*"),
                            "Open directory");
                    if (v.getContext().getPackageManager().resolveActivity(intent, 0) != null) {
                        v.getContext().startActivity(intent);
                    }
                }
            });
        }

        void bindTo(VideoGroup videoGroup) {
            this.videoGroup = videoGroup;
            String[] s = videoGroup.name.split("/");
            String title = s[s.length - 1];
            Spannable spannable = new SpannableString(title
                    + tv_video_group_name.getContext().getString(R.string.video_count, videoGroup.localVideos.size()));
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(tv_video_group_name.getContext(), R.color.light_blue_500)),
                    title.length(), spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv_video_group_name.setText(spannable);

            updateActionMode();
        }

        void updateActionMode() {
            chb_video_group.setVisibility(mAdapter.mInActionMode ? View.VISIBLE : View.GONE);
            chb_video_group.setChecked(mAdapter.mChecked.containsAll(videoGroup.localVideos) && mAdapter.mInActionMode);
        }

        private void checkGroupVideos() {
            if (mAdapter.mChecked.containsAll(videoGroup.localVideos)) {
                mAdapter.mChecked.removeAll(videoGroup.localVideos);
            } else {
                mAdapter.mChecked.addAll(videoGroup.localVideos);
            }
            if (mAdapter.mActionMode != null) {
                mAdapter.mActionMode.setSubtitle(itemView.getContext().getString(R.string.items_count_selected, mAdapter.mChecked.size()));
            }
            TransitionManager.beginDelayedTransition(mAdapter.mParent);
            mAdapter.notifyItemRangeChanged(getAdapterPosition(), videoGroup.localVideos.size() + 1, mAdapter.mInActionMode);
        }
    }

}

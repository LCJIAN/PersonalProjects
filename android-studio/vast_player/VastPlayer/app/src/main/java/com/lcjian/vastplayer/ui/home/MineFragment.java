package com.lcjian.vastplayer.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.ShareFragment;
import com.lcjian.vastplayer.ui.base.BaseFragment;
import com.lcjian.vastplayer.ui.download.DownloadsActivity;
import com.lcjian.vastplayer.ui.mine.FavouriteActivity;
import com.lcjian.vastplayer.ui.mine.SettingsActivity;
import com.lcjian.vastplayer.ui.mine.VideoLibActivity;
import com.lcjian.vastplayer.ui.mine.WatchHistoryActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MineFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.tv_go_favorite)
    TextView tv_go_favorite;
    @BindView(R.id.tv_go_watch_history)
    TextView tv_go_watch_history;
    @BindView(R.id.tv_go_video_library)
    TextView tv_go_video_library;
    @BindView(R.id.tv_go_downloads)
    TextView tv_go_downloads;
    @BindView(R.id.tv_go_share)
    TextView tv_go_share;
    @BindView(R.id.tv_go_settings)
    TextView tv_go_settings;

    @BindView(R.id.fl_go_favorite)
    FrameLayout fl_go_favorite;
    @BindView(R.id.fl_go_watch_history)
    FrameLayout fl_go_watch_history;
    @BindView(R.id.fl_go_video_library)
    FrameLayout fl_go_video_library;
    @BindView(R.id.fl_go_downloads)
    FrameLayout fl_go_downloads;
    @BindView(R.id.fl_go_share)
    FrameLayout fl_go_share;
    @BindView(R.id.fl_go_settings)
    FrameLayout fl_go_settings;

    Unbinder unbinder;

    private int mColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Context context = view.getContext();
        int[] attrs = new int[]{android.R.attr.textColorTertiary};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        mColor = typedArray.getColor(0, ContextCompat.getColor(context, R.color.accent));
        typedArray.recycle();

        fl_go_favorite.setOnClickListener(this);
        fl_go_watch_history.setOnClickListener(this);
        fl_go_video_library.setOnClickListener(this);
        fl_go_downloads.setOnClickListener(this);
        fl_go_share.setOnClickListener(this);
        fl_go_settings.setOnClickListener(this);

        setTextViewLeftDrawable(tv_go_favorite, R.drawable.ic_favorite_white_24dp);
        setTextViewLeftDrawable(tv_go_watch_history, R.drawable.ic_history_white_24dp);
        setTextViewLeftDrawable(tv_go_video_library, R.drawable.ic_video_library_white_24dp);
        setTextViewLeftDrawable(tv_go_downloads, R.drawable.ic_file_download_white_24dp);
        setTextViewLeftDrawable(tv_go_share, R.drawable.ic_share_white_24dp);
        setTextViewLeftDrawable(tv_go_settings, R.drawable.ic_settings_white_24dp);
    }

    private void setTextViewLeftDrawable(TextView textView, int drawableRes) {
        VectorDrawableCompat drawable = VectorDrawableCompat.create(getResources(), drawableRes, textView.getContext().getTheme());
        if (drawable != null) {
            drawable.setTint(mColor);
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_go_favorite:
                startActivity(new Intent(v.getContext(), FavouriteActivity.class));
                break;
            case R.id.fl_go_watch_history:
                startActivity(new Intent(v.getContext(), WatchHistoryActivity.class));
                break;
            case R.id.fl_go_video_library:
                startActivity(new Intent(v.getContext(), VideoLibActivity.class).putExtra("has_content", true));
                break;
            case R.id.fl_go_downloads:
                startActivity(new Intent(v.getContext(), DownloadsActivity.class));
                break;
            case R.id.fl_go_share:
                new ShareFragment().show(getChildFragmentManager(), "ShareFragment");
                break;
            case R.id.fl_go_settings:
                startActivity(new Intent(v.getContext(), SettingsActivity.class));
                break;
            default:
                break;
        }
    }
}

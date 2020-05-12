package com.lcjian.vastplayer.ui.home;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.Genre;
import com.lcjian.vastplayer.data.entity.PageResult;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.ui.base.BaseFragment;
import com.lcjian.vastplayer.ui.base.RecyclerFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MicroVideoFragment extends BaseFragment {

    @BindView(R.id.tab_micro_video)
    TabLayout tab_micro_video;
    @BindView(R.id.vp_micro_video)
    ViewPager vp_micro_video;
    Unbinder unbinder;

    private Observable<List<Genre>> mObservable;

    private Disposable mDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mObservable = mRestAPI.spunSugarService().subjectGenres("video")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_micro_video, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mDisposable = mObservable
                .subscribe(genres -> {
                    vp_micro_video.setAdapter(new MicroVideoPagerAdapter(getChildFragmentManager(), genres));
                    tab_micro_video.setupWithViewPager(vp_micro_video);
                    tab_micro_video.setSelectedTabIndicatorColor(Color.WHITE);
                }, Throwable::printStackTrace);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDisposable.dispose();
        unbinder.unbind();
    }

    private static class MicroVideoPagerAdapter extends FragmentStatePagerAdapter {

        private List<Genre> mData;

        private MicroVideoPagerAdapter(FragmentManager fm, List<Genre> data) {
            super(fm);
            this.mData = data;
        }

        @Override
        public Fragment getItem(int position) {
            return MicroVideosFragment.newInstance(mData.get(position));
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mData.get(position).name;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }
    }

    public static class MicroVideosFragment extends RecyclerFragment<Subject> {

        private MicroVideoAdapter mAdapter;

        private Genre mGenre;

        public static MicroVideosFragment newInstance(Genre genre) {
            MicroVideosFragment fragment = new MicroVideosFragment();
            Bundle args = new Bundle();
            args.putSerializable("genre", genre);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mGenre = (Genre) getArguments().getSerializable("genre");
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Subject> data) {
            mAdapter = new MicroVideoAdapter(data, mRestAPI);
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Subject>> onCreatePageObservable(int currentPage) {
            return mRestAPI.spunSugarService()
                    .subjects("video",
                            null,
                            mGenre.id,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            currentPage,
                            20)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .cache();
        }

        @Override
        public void notifyDataChanged(List<Subject> data) {
            mAdapter.replaceAll(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.primary);

            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recycler_view.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.set(0, 0, 0, (int) DimenUtils.dipToPixels(8, getActivity()));
                }
            });

            super.onViewCreated(view, savedInstanceState);
        }

        @Override
        public String getEmptyMsgResId() {
            return "";
        }

    }
}

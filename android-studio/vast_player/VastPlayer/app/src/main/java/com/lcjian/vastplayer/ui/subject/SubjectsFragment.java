package com.lcjian.vastplayer.ui.subject;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.entity.PageResult;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.ui.base.RecyclerFragment;

import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SubjectsFragment extends RecyclerFragment<Subject> {

    @BindView(R.id.fab_filter)
    FloatingActionButton fab_filter;

    private SubjectAdapter mAdapter;

    private FilterFragment.FilterData mFilterData;

    private CompositeDisposable mDisposables;

    private String mType;

    public static SubjectsFragment newInstance(String type) {
        SubjectsFragment fragment = new SubjectsFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFilterData = new FilterFragment.FilterData();
        if (getArguments() != null) {
            mType = getArguments().getString("type");
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_subjects;
    }

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<Subject> data) {
        mAdapter = new SubjectAdapter(data);
        return mAdapter;
    }

    @Override
    public Observable<PageResult<Subject>> onCreatePageObservable(int currentPage) {
        return mRestAPI.spunSugarService()
                .subjects(mType,
                        null,
                        mFilterData.genre == null ? null : mFilterData.genre.id,
                        mFilterData.country == null ? null : mFilterData.country.id,
                        mFilterData.startReleaseDate,
                        mFilterData.endReleaseDate,
                        mFilterData.startVoteAverage,
                        mFilterData.endVoteAverage,
                        mFilterData.sortType,
                        mFilterData.sortDirection,
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

        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recycler_view.setLayoutManager(gridLayoutManager);
        recycler_view.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (!((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).isFullSpan()) {
                    int pixel1 = (int) DimenUtils.dipToPixels(4, getActivity());
                    int pixel2 = (int) DimenUtils.dipToPixels(8, getActivity());
                    if (((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex() == 0) {
                        outRect.set(pixel2, pixel1, pixel1, pixel1);
                    } else {
                        outRect.set(pixel1, pixel1, pixel2, pixel1);
                    }
                }
            }
        });

        super.onViewCreated(view, savedInstanceState);

        fab_filter.setOnClickListener(view1 -> FilterFragment.newInstance(mFilterData, mType).show(getChildFragmentManager(), "FilterFragment"));

        mDisposables = new CompositeDisposable();
        mDisposables.add(mRxBus.asFlowable()
                .subscribe(event -> {
                    if (event instanceof FilterFragment.FilterData) {
                        mFilterData = (FilterFragment.FilterData) event;
                        setRefreshing(true);
                        refresh();
                    }
                }));
    }

    @Override
    public void onDestroyView() {
        if (mDisposables != null) {
            mDisposables.dispose();
        }
        super.onDestroyView();
    }
}

package com.lcjian.vastplayer.ui.mine;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.db.entity.TvStation;
import com.lcjian.vastplayer.data.entity.PageResult;
import com.lcjian.vastplayer.ui.base.RecyclerFragment;
import com.lcjian.vastplayer.ui.subject.TvStationAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FavouriteTvStationFragment extends RecyclerFragment<TvStation> {

    private TvStationAdapter mAdapter;

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<TvStation> data) {
        mAdapter = new TvStationAdapter(new ArrayList<>(data));
        return mAdapter;
    }

    @Override
    public Observable<PageResult<TvStation>> onCreatePageObservable(int currentPage) {
        return Observable
                .just(currentPage)
                .map(pageNumber -> {
                    PageResult<?> pageResult = new PageResult<>();
                    pageResult.page_number = pageNumber;
                    pageResult.page_size = 20;
                    pageResult.total_elements = mAppDatabase.tvStationDao().getCountSync();
                    pageResult.total_pages = pageResult.total_elements % pageResult.page_size == 0
                            ? pageResult.total_elements / pageResult.page_size
                            : pageResult.total_elements / pageResult.page_size + 1;
                    return pageResult;
                })
                .map(pageResult -> {
                    PageResult<TvStation> favouritePageResult = new PageResult<>();
                    favouritePageResult.page_number = pageResult.page_number;
                    favouritePageResult.page_size = pageResult.page_size;
                    favouritePageResult.total_elements = pageResult.total_elements;
                    favouritePageResult.total_pages = pageResult.total_pages;
                    favouritePageResult.elements = mAppDatabase
                            .tvStationDao()
                            .getPageSync(favouritePageResult.page_size,
                                    (favouritePageResult.page_number - 1) * favouritePageResult.page_size);
                    return favouritePageResult;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    @Override
    public void notifyDataChanged(List<TvStation> data) {
        mAdapter.replaceAll(new ArrayList<>(data));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipe_refresh_layout.setColorSchemeResources(R.color.primary);

        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_view.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offsets = (int) DimenUtils.dipToPixels(4, getActivity());
                outRect.set(offsets, offsets, offsets, offsets);
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    public String getEmptyMsgResId() {
        return getString(R.string.empty_favourites);
    }
}

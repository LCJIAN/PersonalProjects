package com.lcjian.vastplayer.ui.mine;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.db.entity.Favourite;
import com.lcjian.vastplayer.data.entity.PageResult;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.ui.base.RecyclerFragment;
import com.lcjian.vastplayer.ui.subject.SubjectAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FavouriteSubjectsFragment extends RecyclerFragment<Subject> {

    private SubjectAdapter mAdapter;

    private String mType;

    public static FavouriteSubjectsFragment newInstance(String type) {
        FavouriteSubjectsFragment fragment = new FavouriteSubjectsFragment();
        Bundle args = new Bundle();
        args.putString("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString("type");
        }
    }

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<Subject> data) {
        mAdapter = new SubjectAdapter(data);
        return mAdapter;
    }

    @Override
    public Observable<PageResult<Subject>> onCreatePageObservable(int currentPage) {
        return Observable
                .just(currentPage)
                .map(pageNumber -> {
                    PageResult<?> pageResult = new PageResult<>();
                    pageResult.page_number = pageNumber;
                    pageResult.page_size = 20;
                    pageResult.total_elements = mAppDatabase.favouriteDao().getCountByTypeSync(mType);
                    pageResult.total_pages = pageResult.total_elements % pageResult.page_size == 0
                            ? pageResult.total_elements / pageResult.page_size
                            : pageResult.total_elements / pageResult.page_size + 1;
                    return pageResult;
                })
                .map(pageResult -> {
                    PageResult<Favourite> favouritePageResult = new PageResult<>();
                    favouritePageResult.page_number = pageResult.page_number;
                    favouritePageResult.page_size = pageResult.page_size;
                    favouritePageResult.total_elements = pageResult.total_elements;
                    favouritePageResult.total_pages = pageResult.total_pages;
                    favouritePageResult.elements = mAppDatabase
                            .favouriteDao()
                            .getPageByTypeSync(mType, favouritePageResult.page_size,
                                    (favouritePageResult.page_number - 1) * favouritePageResult.page_size);
                    return favouritePageResult;
                })
                .flatMap(favouritePageResult -> {
                    List<Long> ids = new ArrayList<>();
                    for (Favourite favourite : favouritePageResult.elements) {
                        ids.add(favourite.subjectId);
                    }
                    return Observable.zip(
                            mRestAPI.spunSugarService().subjects(TextUtils.join(",", ids)),
                            Observable.just(favouritePageResult),
                            (subjects, pageResultS) -> {
                                PageResult<Subject> pageResult = new PageResult<>();
                                pageResult.page_number = pageResultS.page_number;
                                pageResult.page_size = pageResultS.page_size;
                                pageResult.total_elements = pageResultS.total_elements;
                                pageResult.total_pages = pageResultS.total_pages;
                                pageResult.elements = subjects;
                                for (Subject subject : pageResult.elements) {
                                    for (Favourite favourite : pageResultS.elements) {
                                        if (subject.id.equals(favourite.subjectId)) {
                                            subject.favourite = favourite;
                                            break;
                                        }
                                    }
                                }
                                Collections.sort(pageResult.elements,
                                        (o1, o2) -> (int) (o2.favourite.createTime.getTime() - o1.favourite.createTime.getTime()));
                                return pageResult;
                            });
                })
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

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recycler_view.setLayoutManager(staggeredGridLayoutManager);
        recycler_view.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offsets = (int) DimenUtils.dipToPixels(4, getActivity());
                outRect.set(offsets, offsets, offsets, offsets);
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public String getEmptyMsgResId() {
        return getString(R.string.empty_favourites);
    }
}

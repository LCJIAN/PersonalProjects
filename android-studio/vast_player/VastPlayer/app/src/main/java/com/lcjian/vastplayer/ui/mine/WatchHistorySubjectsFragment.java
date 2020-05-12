package com.lcjian.vastplayer.ui.mine;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.db.entity.WatchHistory;
import com.lcjian.vastplayer.data.entity.PageResult;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.ui.base.RecyclerFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class WatchHistorySubjectsFragment extends RecyclerFragment<Subject> {

    private WatchHistorySubjectAdapter mAdapter;

    private String mType;

    public static WatchHistorySubjectsFragment newInstance(String type) {
        WatchHistorySubjectsFragment fragment = new WatchHistorySubjectsFragment();
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
        mAdapter = new WatchHistorySubjectAdapter(data, mAppDatabase);
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
                    pageResult.total_elements = mAppDatabase
                            .watchHistoryDao().getCountByTypeSync(mType);
                    pageResult.total_pages = pageResult.total_elements % pageResult.page_size == 0
                            ? pageResult.total_elements / pageResult.page_size
                            : pageResult.total_elements / pageResult.page_size + 1;
                    return pageResult;
                })
                .map(pageResult -> {
                    PageResult<WatchHistory> watchHistoryPageResult = new PageResult<>();
                    watchHistoryPageResult.page_number = pageResult.page_number;
                    watchHistoryPageResult.page_size = pageResult.page_size;
                    watchHistoryPageResult.total_elements = pageResult.total_elements;
                    watchHistoryPageResult.total_pages = pageResult.total_pages;
                    watchHistoryPageResult.elements = mAppDatabase
                            .watchHistoryDao().getPageByTypeSync(mType, watchHistoryPageResult.page_size,
                                    (watchHistoryPageResult.page_number - 1) * watchHistoryPageResult.page_size);
                    return watchHistoryPageResult;
                })
                .flatMap(watchHistoryPageResult -> {
                    List<Long> ids = new ArrayList<>();
                    for (WatchHistory watchHistory : watchHistoryPageResult.elements) {
                        ids.add(watchHistory.subjectId);
                    }
                    return Observable.zip(
                            mRestAPI.spunSugarService().subjects(TextUtils.join(",", ids)),
                            Observable.just(watchHistoryPageResult),
                            (subjects, pageResultS) -> {
                                PageResult<Subject> pageResult = new PageResult<>();
                                pageResult.page_number = pageResultS.page_number;
                                pageResult.page_size = pageResultS.page_size;
                                pageResult.total_elements = pageResultS.total_elements;
                                pageResult.total_pages = pageResultS.total_pages;
                                pageResult.elements = subjects;
                                for (Subject subject : pageResult.elements) {
                                    for (WatchHistory watchHistory : pageResultS.elements) {
                                        if (subject.id.equals(watchHistory.subjectId)) {
                                            subject.watchHistory = watchHistory;
                                            break;
                                        }
                                    }
                                }
                                Collections.sort(pageResult.elements,
                                        (o1, o2) -> (int) (o2.watchHistory.updateTime.getTime() - o1.watchHistory.updateTime.getTime()));
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

        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public String getEmptyMsgResId() {
        return getString(R.string.empty_histories);
    }
}
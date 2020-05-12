package com.lcjian.vastplayer.ui.search;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.lib.util.common.PackageUtils2;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.entity.PageResult;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.ui.base.RecyclerFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchResultFragment extends RecyclerFragment<Subject> {

    private SearchResultAdapter mAdapter;

    private String mKeyword;

    public static SearchResultFragment newInstance(String keyword) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putString(Constants.BUNDLE_PARAMETER_KEYWORD, keyword);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<Subject> data) {
        mAdapter = new SearchResultAdapter(null);
        return mAdapter;
    }

    @Override
    public Observable<PageResult<Subject>> onCreatePageObservable(int currentPage) {
        return mRestAPI.spunSugarService().subjects(null, mKeyword, null, null, null,
                null, null, null, null, null, currentPage, 20)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    @Override
    public void notifyDataChanged(List<Subject> data) {
        mAdapter.replaceAll(new ArrayList<>(data));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mKeyword = getArguments().getString(Constants.BUNDLE_PARAMETER_KEYWORD);
        }
        if (TextUtils.equals(mKeyword, PackageUtils2.getAppName(getActivity()))) {
            mUserInfoSp.edit().putBoolean("shared", true).apply();
        }
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
}

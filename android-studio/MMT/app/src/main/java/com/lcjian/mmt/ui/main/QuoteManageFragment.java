package com.lcjian.mmt.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.Quote;
import com.lcjian.mmt.ui.base.BaseFragment;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SimpleFragmentPagerAdapter;
import com.lcjian.mmt.ui.base.SlimAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class QuoteManageFragment extends BaseFragment {

    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.vp)
    ViewPager vp;

    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vp_quote, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vp.setAdapter(new SimpleFragmentPagerAdapter(getChildFragmentManager())
                .addFragment(QuotesFragment.newInstance(1), "待报价")
                .addFragment(QuotesFragment.newInstance(2), "已报价")
                .addFragment(QuotesFragment.newInstance(3), "弃价"));
        vp.setOffscreenPageLimit(3);
        tab.setupWithViewPager(vp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public static class QuotesFragment extends RecyclerFragment<Quote> {

        private Integer mStatus;
        private SlimAdapter mAdapter;

        public static QuotesFragment newInstance(Integer status) {
            QuotesFragment fragment = new QuotesFragment();
            Bundle args = new Bundle();
            args.putInt("status", status);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mStatus = getArguments().getInt("status");
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Quote> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Quote>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.quote_item;
                        }

                        @Override
                        public void onBind(Quote data, SlimAdapter.SlimViewHolder<Quote> viewHolder) {
                            Context context = viewHolder.itemView.getContext();
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Quote>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getQuotes((currentPage - 1) * 20, 20, mStatus)
                    .map(quoteResponsePageData -> {
                        PageResult<Quote> pageResult = new PageResult<>();
                        if (quoteResponsePageData.elements == null) {
                            quoteResponsePageData.elements = new ArrayList<>();
                        }
                        pageResult.elements = quoteResponsePageData.elements;
                        pageResult.page_number = currentPage;
                        pageResult.page_size = 20;
                        pageResult.total_pages = quoteResponsePageData.total_elements % 20 == 0
                                ? quoteResponsePageData.total_elements / 20
                                : quoteResponsePageData.total_elements / 20 + 1;
                        pageResult.total_elements = quoteResponsePageData.total_elements;
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<Quote> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            super.onViewCreated(view, savedInstanceState);
        }
    }
}

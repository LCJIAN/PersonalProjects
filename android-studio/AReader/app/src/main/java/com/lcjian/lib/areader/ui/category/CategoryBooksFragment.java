package com.lcjian.lib.areader.ui.category;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.RxBus;
import com.lcjian.lib.areader.data.entity.Book;
import com.lcjian.lib.areader.data.entity.PageResult;
import com.lcjian.lib.areader.data.entity.RankType;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.network.RestAPI;
import com.lcjian.lib.areader.ui.base.RecyclerFragment;
import com.lcjian.lib.areader.ui.main.BookAdapter;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 分类书籍列表
 *
 * @author LCJIAN
 */
public class CategoryBooksFragment extends RecyclerFragment<Book> {

    private long mChildCategoryId;
    private int mStatus;

    private BookAdapter mAdapter;

    private CompositeDisposable mDisposables;

    public static CategoryBooksFragment newInstance(Long childCategoryId, Integer status) {
        CategoryBooksFragment fragment = new CategoryBooksFragment();
        Bundle args = new Bundle();
        args.putLong("child_category_id", childCategoryId);
        args.putInt("status", status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mChildCategoryId = getArguments().getLong("child_category_id", 0);
            mStatus = getArguments().getInt("status", -1);
        }
    }

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<Book> data) {
        mAdapter = new BookAdapter(data);
        return mAdapter;
    }

    @Override
    public Observable<PageResult<Book>> onCreatePageObservable(final int currentPage) {
        return RestAPI.getInstance().readerService().getBooksByCategory(mChildCategoryId, mStatus, currentPage, 10)
                .map(new Function<ResponseData<List<Book>>, PageResult<Book>>() {

                    @Override
                    public PageResult<Book> apply(ResponseData<List<Book>> listResponseData) {
                        PageResult<Book> result = new PageResult<>();
                        result.page_number = currentPage;
                        result.page_size = 10;
                        result.total_elements = Integer.MAX_VALUE;
                        result.total_pages = listResponseData.data.isEmpty() ? currentPage : currentPage + 1;
                        result.elements = listResponseData.data;
                        return result;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void notifyDataChanged(List<Book> data) {
        mAdapter.replaceAll(data);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);

        recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        super.onViewCreated(view, savedInstanceState);

        mDisposables = new CompositeDisposable();
        mDisposables.add(RxBus.getInstance().asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object event) {
                        if (event instanceof RankType) {
                            mStatus = ((RankType) event).type;
                            recycler_view.post(new Runnable() {
                                @Override
                                public void run() {
                                    refresh();
                                }
                            });
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                }));
    }

    @Override
    public void onDestroyView() {
        mDisposables.dispose();
        super.onDestroyView();
    }
}

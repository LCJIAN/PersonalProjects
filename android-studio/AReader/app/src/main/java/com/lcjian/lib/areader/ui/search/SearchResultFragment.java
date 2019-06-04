package com.lcjian.lib.areader.ui.search;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;

import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.data.entity.Book;
import com.lcjian.lib.areader.data.entity.PageResult;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.network.RestAPI;
import com.lcjian.lib.areader.ui.base.RecyclerFragment;
import com.lcjian.lib.areader.ui.main.BookAdapter;

import java.nio.charset.Charset;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 搜索结果列表
 *
 * @author LCJIAN
 */
public class SearchResultFragment extends RecyclerFragment<Book> {

    private String mKeyword;

    private BookAdapter mAdapter;

    public static SearchResultFragment newInstance(String keyword) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putString("keyword", keyword);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mKeyword = getArguments().getString("keyword");
        }
    }

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<Book> data) {
        mAdapter = new BookAdapter(data);
        return mAdapter;
    }

    @Override
    public Observable<PageResult<Book>> onCreatePageObservable(final int currentPage) {
        return RestAPI.getInstance().readerService()
                .search(RequestBody.create(MediaType.parse("application/json"),
                        "{\"data\":{\"q\":\"" +
                                Base64.encodeToString(mKeyword.getBytes(Charset.defaultCharset()), Base64.NO_WRAP) +
                                "\",\"t\":2}}"))
                .map(new Function<ResponseData<List<Book>>, PageResult<Book>>() {
                    @Override
                    public PageResult<Book> apply(ResponseData<List<Book>> listResponseData) {
                        PageResult<Book> result = new PageResult<>();
                        result.page_number = currentPage;
                        result.page_size = 10;
                        result.total_elements = Integer.MAX_VALUE;
                        result.total_pages = 1;
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
        TypedArray typedArray = view.getContext().getTheme()
                .obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
        swipe_refresh_layout.setBackgroundDrawable(typedArray.getDrawable(0));

        recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        super.onViewCreated(view, savedInstanceState);
    }
}

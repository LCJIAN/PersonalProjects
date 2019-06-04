package com.lcjian.lib.areader.ui.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.RxBus;
import com.lcjian.lib.areader.data.entity.Book;
import com.lcjian.lib.areader.data.entity.BookGroup;
import com.lcjian.lib.areader.data.entity.Displayable;
import com.lcjian.lib.areader.data.entity.PageResult;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.network.RestAPI;
import com.lcjian.lib.areader.ui.base.RecyclerFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * 书城
 *
 * @author LCJIAN
 */
public class BookStoreFragment extends RecyclerFragment<Displayable> {

    private BookStoreAdapter mAdapter;

    private CompositeDisposable mDisposables;

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<Displayable> data) {
        mAdapter = new BookStoreAdapter(data);
        return mAdapter;
    }

    @Override
    public Observable<PageResult<Displayable>> onCreatePageObservable(final int currentPage) {
        if (currentPage == 1) {
            return Observable.zip(RestAPI.getInstance().readerService().rank(2),
                    RestAPI.getInstance().readerService().guessYouLike(1, 10),
                    new BiFunction<ResponseData<BookGroup>, ResponseData<List<Book>>, PageResult<Displayable>>() {
                        @Override
                        public PageResult<Displayable> apply(ResponseData<BookGroup> o, ResponseData<List<Book>> listResponseData) {
                            PageResult<Displayable> result = new PageResult<>();
                            result.page_number = 1;
                            result.page_size = 10;
                            result.total_elements = Integer.MAX_VALUE;
                            result.total_pages = listResponseData.data.isEmpty() ? currentPage : currentPage + 1;
                            result.elements = new ArrayList<>();
                            result.elements.add(o.data);

                            BookGroup.GroupStartItem hot = new BookGroup.GroupStartItem("精品热推", o.data.hot, 6, 1);
                            result.elements.add(hot);
                            result.elements.addAll(hot.getShowData());
                            result.elements.add(new BookGroup.GroupEndItem("换一换"));

                            BookGroup.GroupStartItem today = new BookGroup.GroupStartItem("精品新书", o.data.today, 3, 0);
                            result.elements.add(today);
                            result.elements.addAll(today.getShowData());
                            result.elements.add(new BookGroup.GroupEndItem("换一换"));

                            BookGroup.GroupStartItem boy = new BookGroup.GroupStartItem("超强男频", o.data.boy, 6, 1);
                            result.elements.add(boy);
                            result.elements.addAll(boy.getShowData());
                            result.elements.add(new BookGroup.GroupEndItem("换一换"));

                            BookGroup.GroupStartItem boyEnd = new BookGroup.GroupStartItem("男频完结好书", o.data.boyEnd, 3, 0);
                            result.elements.add(boyEnd);
                            result.elements.addAll(boyEnd.getShowData());
                            result.elements.add(new BookGroup.GroupEndItem("换一换"));

                            BookGroup.GroupStartItem girl = new BookGroup.GroupStartItem("最美女频", o.data.girl, 6, 1);
                            result.elements.add(girl);
                            result.elements.addAll(girl.getShowData());
                            result.elements.add(new BookGroup.GroupEndItem("换一换"));

                            BookGroup.GroupStartItem girlEnd = new BookGroup.GroupStartItem("女频完结好书", o.data.girlEnd, 3, 0);
                            result.elements.add(girlEnd);
                            result.elements.addAll(girlEnd.getShowData());
                            result.elements.add(new BookGroup.GroupEndItem("换一换"));

                            result.elements.add(new BookGroup.GroupStartItem("猜你喜欢", null, 0, 0));
                            result.elements.addAll(listResponseData.data);
                            return result;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return RestAPI.getInstance().readerService()
                    .guessYouLike(currentPage, 10)
                    .map(new Function<ResponseData<List<Book>>, PageResult<Displayable>>() {

                        @Override
                        public PageResult<Displayable> apply(ResponseData<List<Book>> listResponseData) {
                            PageResult<Displayable> result = new PageResult<>();
                            result.page_number = currentPage;
                            result.page_size = 10;
                            result.total_elements = Integer.MAX_VALUE;
                            result.total_pages = listResponseData.data.isEmpty() ? currentPage : currentPage + 1;
                            result.elements = new ArrayList<>();
                            result.elements.addAll(listResponseData.data);
                            return result;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == RecyclerView.NO_POSITION) {
                    return 3;
                }
                return mAdapter.getItemViewType(position) == 3 ? 1 : 3;
            }
        });
        recycler_view.setLayoutManager(gridLayoutManager);

        super.onViewCreated(view, savedInstanceState);

        mDisposables = new CompositeDisposable();
        mDisposables.add(RxBus.getInstance().asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object event) {
                        if (event instanceof BookGroup.GroupEndItem) {
                            int to = mAdapter.getData().indexOf(event);
                            int from = to;
                            BookGroup.GroupStartItem gs;
                            while (true) {
                                from--;
                                Displayable d = mAdapter.getData().get(from);
                                if (d instanceof BookGroup.GroupStartItem) {
                                    gs = (BookGroup.GroupStartItem) d;
                                    break;
                                }
                            }
                            mAdapter.getData().removeAll(new ArrayList<>(mAdapter.getData().subList(from + 1, to)));
                            mAdapter.getData().addAll(from + 1, gs.getShowData());
                            mAdapter.notifyItemRangeChanged(from + 1, gs.getShowData().size());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Timber.e(throwable);
                    }
                }));
    }

    @Override
    public void notifyDataChanged(List<Displayable> data) {
        mAdapter.replaceAll(data);
    }

    @Override
    public void onDestroyView() {
        mDisposables.dispose();
        super.onDestroyView();
    }
}

package com.lcjian.lib.areader.ui.main;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.example.newbiechen.ireader.model.bean.BookChapterBean;
import com.example.newbiechen.ireader.model.bean.CollBookBean;
import com.example.newbiechen.ireader.ui.activity.ReadActivity;
import com.lcjian.lib.areader.App;
import com.lcjian.lib.areader.Constants;
import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.RxBus;
import com.lcjian.lib.areader.data.db.entity.BookHistory;
import com.lcjian.lib.areader.data.db.entity.Bookshelf;
import com.lcjian.lib.areader.data.entity.Book;
import com.lcjian.lib.areader.data.entity.BookReadInfo;
import com.lcjian.lib.areader.data.entity.BooksReadInfo;
import com.lcjian.lib.areader.data.entity.Chapter;
import com.lcjian.lib.areader.data.entity.PageResult;
import com.lcjian.lib.areader.data.entity.RequestData;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.network.RestAPI;
import com.lcjian.lib.areader.ui.base.LoadMoreAdapter;
import com.lcjian.lib.areader.ui.base.RecyclerFragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 书架
 *
 * @author LCJIAN
 */
public class BookshelfFragment extends RecyclerFragment<Book> {

    private BookshelfAdapter mAdapter;
    private LoadMoreAdapter mLoadMoreAdapter;

    private GridLayoutManager mGridLayoutManager;

    private View mCurrentFooter;

    private CompositeDisposable mDisposables;

    private DecimalFormat df = new DecimalFormat("0.00%");

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<Book> data) {
        mAdapter = new BookshelfAdapter(data, mGridLayoutManager);
        return mAdapter;
    }

    @Override
    public void onLoadMoreAdapterCreated(LoadMoreAdapter loadMoreAdapter) {
        mLoadMoreAdapter = loadMoreAdapter;
        addFooter(R.layout.book_shelf_grid_item, false);
    }

    @Override
    public Observable<PageResult<Book>> onCreatePageObservable(final int currentPage) {
        Observable<List<Bookshelf>> observableBookshelf = Observable
                .defer(new Callable<Observable<List<Bookshelf>>>() {
                    @Override
                    public Observable<List<Bookshelf>> call() {
                        List<Bookshelf> bookshelves = App.getInstance().getDataBase()
                                .bookshelfDao().getAll();
                        return Observable.just(bookshelves);
                    }
                });
        Observable<List<BookHistory>> observableHistories = Observable
                .defer(new Callable<Observable<List<BookHistory>>>() {
                    @Override
                    public Observable<List<BookHistory>> call() {
                        List<BookHistory> bookHistories = App.getInstance().getDataBase()
                                .bookHistoryDao().getAll();
                        return Observable.just(bookHistories);
                    }
                }).cache();
        Observable<PageResult<Book>> observableBooks = Observable.zip(observableBookshelf,
                observableHistories,
                new BiFunction<List<Bookshelf>, List<BookHistory>, RequestData<BooksReadInfo>>() {
                    @Override
                    public RequestData<BooksReadInfo> apply(List<Bookshelf> bookshelves, List<BookHistory> bookHistories) {
                        RequestData<BooksReadInfo> requestData = new RequestData<>();
                        requestData.data = new BooksReadInfo();
                        requestData.data.devId = Constants.DEVICE_ID;
                        requestData.data.books = new ArrayList<>();
                        for (Bookshelf bookshelf : bookshelves) {
                            BookReadInfo bookReadInfo = new BookReadInfo();
                            bookReadInfo.bookId = bookshelf.bookId;
                            for (BookHistory bookHistory : bookHistories) {
                                if (bookshelf.bookId.equals(bookHistory.bookId)) {
                                    bookReadInfo.readTime = bookHistory.readTime;
                                    break;
                                }
                            }
                            requestData.data.books.add(bookReadInfo);
                        }
                        return requestData;
                    }
                })
                .flatMap(new Function<RequestData<BooksReadInfo>, Observable<ResponseData<List<Book>>>>() {
                    @Override
                    public Observable<ResponseData<List<Book>>> apply(RequestData<BooksReadInfo> booksReadInfoRequestData) {
                        return RestAPI.getInstance().readerService().getBookshelf(booksReadInfoRequestData);
                    }
                })
                .map(new Function<ResponseData<List<Book>>, PageResult<Book>>() {
                    @Override
                    public PageResult<Book> apply(ResponseData<List<Book>> listResponseData) {
                        PageResult<Book> result = new PageResult<>();
                        result.page_number = 1;
                        result.page_size = 10;
                        result.total_elements = listResponseData.data.size();
                        result.total_pages = 1;
                        result.elements = listResponseData.data;
                        return result;
                    }
                });
        return Observable.zip(observableHistories, observableBooks,
                new BiFunction<List<BookHistory>, PageResult<Book>, PageResult<Book>>() {
                    @Override
                    public PageResult<Book> apply(List<BookHistory> bookHistories, PageResult<Book> bookPageResult) throws Exception {
                        for (Book book : bookPageResult.elements) {
                            for (BookHistory bookHistory : bookHistories) {
                                if (book.id.equals(bookHistory.bookId)) {
                                    book.progress = df.format(((double) bookHistory.chapterIndex) / book.lastIndex);
                                    break;
                                }
                            }
                        }
                        return bookPageResult;
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
        swipe_refresh_layout.setBackgroundColor(ContextCompat.getColor(view.getContext(), android.R.color.white));

        mGridLayoutManager = new GridLayoutManager(view.getContext(), 3);
        recycler_view.setLayoutManager(mGridLayoutManager);
        super.onViewCreated(view, savedInstanceState);

        mDisposables = new CompositeDisposable();
        mDisposables.add(RxBus.getInstance()
                .asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object event) {
                        if (event instanceof Book) {
                            goToRead((Book) event);
                        } else if (event instanceof Boolean) {
                            mAdapter.setCheckMode((Boolean) event);
                            if ((Boolean) event) {
                                if (mCurrentFooter != null) {
                                    mLoadMoreAdapter.removeFooter(mCurrentFooter);
                                }
                            } else {
                                addFooter(R.layout.book_shelf_grid_item, false);
                            }
                        } else if (event instanceof String) {
                            if (TextUtils.equals("select_all_bookshelf", (CharSequence) event)) {
                                mAdapter.getChecked().clear();
                                mAdapter.getChecked().addAll(mAdapter.getData());
                                mAdapter.notifyDataSetChanged();
                            } else if (TextUtils.equals("delete_bookshelf", (CharSequence) event)) {
                                List<Bookshelf> bookshelves = new ArrayList<>();
                                for (Book book : mAdapter.getChecked()) {
                                    Bookshelf bookshelf = new Bookshelf();
                                    bookshelf.bookId = book.id;
                                    bookshelves.add(bookshelf);
                                }
                                App.getInstance().getDataBase().bookshelfDao().delete(bookshelves.toArray(new Bookshelf[bookshelves.size()]));
                                refresh();
                            } else if (TextUtils.equals("clean_bookshelf_check", (CharSequence) event)) {
                                mAdapter.getChecked().clear();
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                }));


    }

    @Override
    public void onResume() {
        super.onResume();
        mDisposables.add(Observable.zip(
                Observable.just(App.getInstance().getDataBase().bookHistoryDao().getAll()),
                Observable.just(App.getInstance().getDataBase().bookshelfDao().getAll()),
                (t1, t2) -> true)
                .subscribe(aBoolean -> refresh(),
                        throwable -> {

                        }));
    }

    @Override
    public void onDestroyView() {
        mDisposables.dispose();
        super.onDestroyView();
    }

    private void addFooter(@LayoutRes int footerLayout, boolean singleLine) {
        if (mCurrentFooter != null) {
            mLoadMoreAdapter.removeFooter(mCurrentFooter);
        }
        View footer = LayoutInflater.from(getContext()).inflate(footerLayout, recycler_view, false);
        ((ImageView) footer.findViewById(R.id.iv_book_poster)).setImageResource(R.drawable.add_book_flag);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).check(R.id.rb_book_store);
            }
        });
        mLoadMoreAdapter.addFooter(footer, singleLine);
        mCurrentFooter = footer;
    }

    private void goToRead(Book book) {
        mDisposables.add(RestAPI.getInstance().readerService().getBookChapters(book.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseData<List<Chapter>>>() {
                    @Override
                    public void accept(ResponseData<List<Chapter>> listResponseData) {
                        CollBookBean bookHistory = new CollBookBean();
                        bookHistory.set_id(String.valueOf(book.id));
                        bookHistory.setTitle(book.name);
                        bookHistory.setAuthor(book.author);

                        bookHistory.setChaptersCount(listResponseData.data.size());
                        bookHistory.setBookChapters(new ArrayList<>());
                        for (Chapter chapter : listResponseData.data) {
                            BookChapterBean chapterBean = new BookChapterBean();
                            chapterBean.setBookId(String.valueOf(book.id));
                            chapterBean.setTitle(chapter.name);
                            chapterBean.setLink(String.valueOf(chapter.index));
                            bookHistory.getBookChapterList().add(chapterBean);
                        }
                        boolean isCollected = !App.getInstance().getDataBase().bookshelfDao().getBookshelvesByBookIds(Collections.singletonList(book.id)).isEmpty();
                        ReadActivity.startActivity(getContext(), bookHistory, isCollected);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                }));
    }
}

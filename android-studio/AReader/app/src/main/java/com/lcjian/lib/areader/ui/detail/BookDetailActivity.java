package com.lcjian.lib.areader.ui.detail;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.newbiechen.ireader.model.bean.BookChapterBean;
import com.example.newbiechen.ireader.model.bean.CollBookBean;
import com.example.newbiechen.ireader.ui.activity.ReadActivity;
import com.lcjian.lib.areader.App;
import com.lcjian.lib.areader.Global;
import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.data.db.entity.Bookshelf;
import com.lcjian.lib.areader.data.entity.Book;
import com.lcjian.lib.areader.data.entity.BookDetailResult;
import com.lcjian.lib.areader.data.entity.Chapter;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.network.RestAPI;
import com.lcjian.lib.areader.ui.base.BaseActivity;
import com.lcjian.lib.areader.util.DateUtils;
import com.lcjian.lib.areader.util.DimenUtils;
import com.lcjian.lib.areader.widget.MyScrollView;
import com.lcjian.lib.areader.widget.RatioLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.blurry.Blurry;

/**
 * 书籍详情
 *
 * @author LCJIAN
 */
public class BookDetailActivity extends BaseActivity implements View.OnClickListener, MyScrollView.OnScrollChangeListener {

    private static int padding = (int) DimenUtils.dipToPixels(8, App.getInstance());
    @BindView(R.id.sv_book_detail)
    MyScrollView sv_book_detail;
    @BindView(R.id.rl_top_bar)
    RelativeLayout rl_top_bar;
    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.rl_book_poster_land)
    RatioLayout rl_book_poster_land;
    @BindView(R.id.iv_book_poster_land)
    ImageView iv_book_poster_land;
    @BindView(R.id.iv_book_poster)
    ImageView iv_book_poster;
    @BindView(R.id.tv_book_name)
    TextView tv_book_name;
    @BindView(R.id.tv_book_author)
    TextView tv_book_author;
    @BindView(R.id.tv_book_status)
    TextView tv_book_status;
    @BindView(R.id.tv_book_introduction)
    TextView tv_book_introduction;
    @BindView(R.id.ll_book_last_info)
    LinearLayout ll_book_last_info;
    @BindView(R.id.tv_book_last_info)
    TextView tv_book_last_info;
    @BindView(R.id.tv_book_status_2)
    TextView tv_book_status_2;
    @BindView(R.id.fl_book_1)
    FrameLayout fl_book_1;
    @BindView(R.id.fl_book_2)
    FrameLayout fl_book_2;
    @BindView(R.id.fl_book_3)
    FrameLayout fl_book_3;
    @BindView(R.id.fl_book_4)
    FrameLayout fl_book_4;
    @BindView(R.id.fl_book_5)
    FrameLayout fl_book_5;
    @BindView(R.id.fl_book_6)
    FrameLayout fl_book_6;
    @BindView(R.id.fl_book_7)
    FrameLayout fl_book_7;
    @BindView(R.id.fl_book_8)
    FrameLayout fl_book_8;
    @BindView(R.id.tv_add_to_bookshelf)
    TextView tv_add_to_bookshelf;
    @BindView(R.id.tv_read_now)
    TextView tv_read_now;

    private Book mBook;
    private boolean mLoaded;
    private Disposable mDisposable;
    private Disposable mDisposable2;

    private long mBookId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        ButterKnife.bind(this);

        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(this);
        ll_book_last_info.setOnClickListener(this);
        tv_add_to_bookshelf.setOnClickListener(this);
        tv_read_now.setOnClickListener(this);
        sv_book_detail.setOnScrollChangeListenerFor(this);

        mBookId = getIntent().getLongExtra("book_id", 0);
        refresh(mBookId);
    }

    private void refresh(Long bookId) {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = RestAPI.getInstance().readerService()
                .getBookDetail(bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseData<BookDetailResult>>() {
                    @Override
                    public void accept(ResponseData<BookDetailResult> bookDetailResultResponseData) {
                        mBook = bookDetailResultResponseData.data.detail;
                        setupDetail();
                        if (!mLoaded) {
                            List<Book> allLikeBooks = bookDetailResultResponseData.data.allLikeBooks;
                            List<Book> similarBooks = bookDetailResultResponseData.data.similarBooks;
                            setupBookItem(fl_book_1, allLikeBooks.get(0));
                            setupBookItem(fl_book_2, allLikeBooks.get(1));
                            setupBookItem(fl_book_3, allLikeBooks.get(2));
                            setupBookItem(fl_book_4, allLikeBooks.get(3));
                            setupBookItem(fl_book_5, similarBooks.get(0));
                            setupBookItem(fl_book_6, similarBooks.get(1));
                            setupBookItem(fl_book_7, similarBooks.get(2));
                            setupBookItem(fl_book_8, similarBooks.get(3));
                            mLoaded = true;
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                });
    }

    private void setupDetail() {
        Glide.with(iv_book_poster_land)
                .load(mBook.poster)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        iv_book_poster_land.post(new Runnable() {
                            @Override
                            public void run() {
                                Blurry.delete(rl_book_poster_land);
                                Blurry.with(BookDetailActivity.this)
                                        .radius(25)
                                        .sampling(2)
                                        .color(0x33000000)
                                        .async()
                                        .animate(500)
                                        .onto(rl_book_poster_land);
                            }
                        });
                        return false;
                    }
                })
                .into(iv_book_poster_land);
        Glide.with(iv_book_poster)
                .load(mBook.poster)
                .apply(Global.roundedPoster2)
                .into(iv_book_poster);
        tv_book_name.setText(mBook.name);
        tv_book_author.setText(mBook.author);

        String status = (mBook.status == 0 ? getString(R.string.un_complete) : getString(R.string.complete)) + " | " + mBook.categoryName;
        tv_book_status.setText(status);
        tv_book_introduction.setText(mBook.introduction);

        String lastInfo = DateUtils.getRelativeTimeStr(new Date(mBook.lastUpTime * 1000))
                + " " + getString(R.string.update_to) + mBook.lastName;
        tv_book_last_info.setText(lastInfo);
        tv_book_status_2.setText(mBook.status == 0 ? R.string.un_complete : R.string.complete);
    }

    private void setupBookItem(FrameLayout fl, final Book book) {
        LinearLayout ll_book_grid = fl.findViewById(R.id.ll_book_grid);
        ImageView book_poster = fl.findViewById(R.id.iv_book_poster);
        TextView book_name = fl.findViewById(R.id.tv_book_name);
        TextView book_author = fl.findViewById(R.id.tv_book_author);
        Glide.with(book_poster)
                .load(book.poster)
                .apply(Global.roundedPoster2)
                .into(book_poster);
        book_name.setText(book.name);
        book_author.setText(book.author);
        ll_book_grid.setPadding(padding, padding, padding, padding);

        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh(book.id);
            }
        });
    }

    @Override
    protected void onResume() {
        List<Bookshelf> list = App.getInstance().getDataBase().bookshelfDao()
                .getBookshelvesByBookIds(Collections.singletonList(mBookId));
        if (list.isEmpty()) {
            tv_add_to_bookshelf.setText(R.string.add_to_bookshelf);
        } else {
            tv_add_to_bookshelf.setText(R.string.remove_from_bookshelf);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposable2 != null) {
            mDisposable2.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.ll_book_last_info:
                if (mBook != null) {
                    ChapterDialogFragment.newInstance(mBook).show(getSupportFragmentManager(), "ChapterDialogFragment");
                }
                break;
            case R.id.tv_add_to_bookshelf:
                if (mBook != null) {
                    Bookshelf bookshelf;
                    List<Bookshelf> list = App.getInstance().getDataBase().bookshelfDao()
                            .getBookshelvesByBookIds(Collections.singletonList(mBook.id));
                    if (list.isEmpty()) {
                        bookshelf = new Bookshelf();
                        bookshelf.bookId = mBook.id;
                        bookshelf.bookName = mBook.name;
                        App.getInstance().getDataBase().bookshelfDao()
                                .insert(bookshelf);

                        tv_add_to_bookshelf.setText(R.string.remove_from_bookshelf);
                    } else {
                        bookshelf = list.get(0);
                        App.getInstance().getDataBase().bookshelfDao()
                                .delete(bookshelf);

                        tv_add_to_bookshelf.setText(R.string.add_to_bookshelf);
                    }
                }
                break;
            case R.id.tv_read_now:
                if (mBook != null) {
                    goToRead(mBook);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onScrollChange(MyScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (scrollY < 2) {
            rl_top_bar.setVisibility(View.INVISIBLE);
        } else {
            rl_top_bar.setVisibility(View.VISIBLE);
        }
    }

    private void goToRead(Book book) {
        mDisposable2 = RestAPI.getInstance().readerService().getBookChapters(book.id)
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
                        ReadActivity.startActivity(BookDetailActivity.this, bookHistory, isCollected);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                });

    }
}

package com.lcjian.lib.areader.ui.detail;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.newbiechen.ireader.model.bean.BookChapterBean;
import com.example.newbiechen.ireader.model.bean.CollBookBean;
import com.example.newbiechen.ireader.ui.activity.ReadActivity;
import com.lcjian.lib.areader.App;
import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.RxBus;
import com.lcjian.lib.areader.data.entity.Book;
import com.lcjian.lib.areader.data.entity.Chapter;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.network.RestAPI;
import com.lcjian.lib.areader.ui.base.SlidingDialog;
import com.lcjian.lib.areader.util.DimenUtils;
import com.lcjian.lib.areader.util.Spans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 章节列表
 *
 * @author LCJIAN
 */
public class ChapterDialogFragment extends DialogFragment {

    @BindView(R.id.tv_book_chapter_count)
    TextView tv_book_chapter_count;
    @BindView(R.id.ll_book_chapter_sort_order)
    LinearLayout ll_book_chapter_sort_order;
    @BindView(R.id.tv_book_chapter_sort_order)
    TextView tv_book_chapter_sort_order;
    @BindView(R.id.iv_book_chapter_sort_order)
    ImageView iv_book_chapter_sort_order;
    @BindView(R.id.rv_chapter)
    RecyclerView rv_chapter;

    private Unbinder mUnBinder;

    private Book mBook;

    private List<Chapter> chapters;
    private ChapterAdapter mAdapter;

    private CompositeDisposable mDisposables;

    public static ChapterDialogFragment newInstance(Book book) {
        ChapterDialogFragment fragment = new ChapterDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("book", book);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBook = (Book) getArguments().getSerializable("book");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new SlidingDialog(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapter_dialog, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ll_book_chapter_sort_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.equals(tv_book_chapter_sort_order.getText(), getString(R.string.asc_order))) {
                    tv_book_chapter_sort_order.setText(R.string.desc_order);
                    iv_book_chapter_sort_order.setRotation(0);
                } else {
                    tv_book_chapter_sort_order.setText(R.string.asc_order);
                    iv_book_chapter_sort_order.setRotation(180);
                }

                if (chapters != null) {
                    Collections.reverse(chapters);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        rv_chapter.setHasFixedSize(true);
        rv_chapter.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mDisposables = new CompositeDisposable();
        mDisposables.add(RxBus.getInstance()
                .asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(final Object event) {
                        if (event instanceof Chapter) {
                            CollBookBean bookHistory = new CollBookBean();
                            bookHistory.set_id(String.valueOf(mBook.id));
                            bookHistory.setTitle(mBook.name);
                            bookHistory.setAuthor(mBook.author);

                            bookHistory.setChaptersCount(chapters.size());
                            bookHistory.setBookChapters(new ArrayList<>());
                            for (Chapter chapter : chapters) {
                                BookChapterBean chapterBean = new BookChapterBean();
                                chapterBean.setBookId(String.valueOf(mBook.id));
                                chapterBean.setTitle(chapter.name);
                                chapterBean.setLink(String.valueOf(chapter.index));
                                bookHistory.getBookChapterList().add(chapterBean);
                            }
                            boolean isCollected = !App.getInstance().getDataBase().bookshelfDao().getBookshelvesByBookIds(Collections.singletonList(mBook.id)).isEmpty();
                            ReadActivity.startActivity(getContext(), bookHistory, chapters.indexOf(event), isCollected);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                }));

        mDisposables.add(RestAPI.getInstance().readerService().getBookChapters(mBook.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseData<List<Chapter>>>() {
                    @Override
                    public void accept(ResponseData<List<Chapter>> listResponseData) {
                        String status = mBook.status == 0 ? getString(R.string.un_complete) : getString(R.string.complete);
                        String chapterCount = getString(R.string.chapter_count, mBook.lastIndex);
                        String str = status + "  " + chapterCount;
                        tv_book_chapter_count.setText(str);

                        chapters = listResponseData.data;
                        mAdapter = new ChapterAdapter(chapters);
                        rv_chapter.setAdapter(mAdapter);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
        mDisposables.dispose();
    }

    static class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {

        private List<Chapter> mData;


        ChapterAdapter(List<Chapter> data) {
            this.mData = data;
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        @NonNull
        @Override
        public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ChapterViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
            holder.bindTo(mData.get(position));
        }

        static class ChapterViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_chapter_name)
            TextView tv_chapter_name;

            Chapter chapter;

            ChapterViewHolder(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_item, parent, false));
                ButterKnife.bind(this, this.itemView);
                this.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RxBus.getInstance().send(chapter);
                    }
                });
            }

            void bindTo(Chapter c) {
                this.chapter = c;
                tv_chapter_name.setText(new Spans()
                        .append(String.valueOf(chapter.index) + ".  ",
                                new ForegroundColorSpan(ContextCompat.getColor(itemView.getContext(), R.color.colorTextLightGray)),
                                new AbsoluteSizeSpan(DimenUtils.spToPixels(12, itemView.getContext())))
                        .append(chapter.name));
            }
        }
    }
}

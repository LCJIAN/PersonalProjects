package com.lcjian.lib.areader.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.lib.areader.App;
import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.RxBus;
import com.lcjian.lib.areader.data.entity.Displayable;
import com.lcjian.lib.areader.data.entity.PageResult;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.entity.SearchKeyword;
import com.lcjian.lib.areader.data.entity.SearchKeywordGroup;
import com.lcjian.lib.areader.data.network.RestAPI;
import com.lcjian.lib.areader.ui.base.RecyclerFragment;
import com.lcjian.lib.areader.util.Spans;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 搜索关键字
 *
 * @author LCJIAN
 */
public class SearchKeywordFragment extends RecyclerFragment<Displayable> {

    private SearchKeywordAdapter mAdapter;

    private CompositeDisposable mDisposables;

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<Displayable> data) {
        mAdapter = new SearchKeywordAdapter(data);
        return mAdapter;
    }

    @Override
    public Observable<PageResult<Displayable>> onCreatePageObservable(int currentPage) {
        return Observable.zip(
                RestAPI.getInstance().readerService()
                        .hotSearch(RequestBody.create(MediaType.parse("application/json"), "{\"data\":{\"t\":0}}")),
                Observable.defer(new Callable<Observable<List<SearchKeyword>>>() {
                    @Override
                    public Observable<List<SearchKeyword>> call() throws Exception {
                        return Observable.fromArray(App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE)
                                .getString("search_history", "").split(","))
                                .filter(new Predicate<String>() {

                                    @Override
                                    public boolean test(String s) {
                                        return !TextUtils.isEmpty(s);
                                    }
                                })
                                .map(new Function<String, SearchKeyword>() {

                                    @Override
                                    public SearchKeyword apply(String s) {
                                        SearchKeyword sk = new SearchKeyword();
                                        sk.bookName = s;
                                        return sk;
                                    }
                                })
                                .toList()
                                .toObservable();
                    }
                }),
                new BiFunction<ResponseData<List<SearchKeyword>>, List<SearchKeyword>, PageResult<Displayable>>() {

                    @Override
                    public PageResult<Displayable> apply(ResponseData<List<SearchKeyword>> listResponseData, List<SearchKeyword> searchKeywords) throws Exception {
                        PageResult<Displayable> result = new PageResult<>();
                        List<Displayable> elements = new ArrayList<>();
                        elements.add(new SearchKeywordGroup("热们搜索", R.drawable.ic_refresh));
                        elements.addAll(listResponseData.data);
                        if (searchKeywords != null && !searchKeywords.isEmpty()) {
                            elements.add(new SearchKeywordGroup("历史记录", R.drawable.ic_delete));
                            elements.addAll(searchKeywords);
                        }
                        result.page_number = 1;
                        result.page_size = elements.size();
                        result.total_elements = elements.size();
                        result.total_pages = 1;
                        result.elements = elements;
                        return result;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void notifyDataChanged(List<Displayable> data) {
        mAdapter.replaceAll(data);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == RecyclerView.NO_POSITION) {
                    return 2;
                }
                return mAdapter.getItemViewType(position) == 0 ? 2 : 1;
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
                        if (event instanceof SearchKeywordGroup) {
                            if (((SearchKeywordGroup) event).iconResource == R.drawable.ic_delete) {
                                App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE)
                                        .edit()
                                        .remove("search_history")
                                        .apply();
                            }
                            refresh();
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

    static class SearchKeywordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_GROUP = 0;
        private static final int TYPE_KEYWORD = 1;

        private List<Displayable> mData;

        SearchKeywordAdapter(List<Displayable> data) {
            this.mData = data;
        }

        void replaceAll(final List<Displayable> data) {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {

                @Override
                public int getOldListSize() {
                    return mData == null ? 0 : mData.size();
                }

                @Override
                public int getNewListSize() {
                    return data == null ? 0 : data.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mData.get(oldItemPosition).equals(data.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return mData.get(oldItemPosition).equals(data.get(newItemPosition));
                }
            }, true);
            mData = data;
            diffResult.dispatchUpdatesTo(this);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (mData.get(position) instanceof SearchKeywordGroup) {
                return TYPE_GROUP;
            } else {
                return TYPE_KEYWORD;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_GROUP:
                    return new KeywordGroupViewHolder(parent);
                case TYPE_KEYWORD:
                    return new KeywordViewHolder(parent);
                default:
                    return new KeywordViewHolder(parent);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof KeywordGroupViewHolder) {
                ((KeywordGroupViewHolder) holder).bindTo((SearchKeywordGroup) mData.get(position), position);
            } else if (holder instanceof KeywordViewHolder) {
                ((KeywordViewHolder) holder).bindTo((SearchKeyword) mData.get(position), position < 3);
            }
        }

        static class KeywordGroupViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.v_divider)
            View v_divider;
            @BindView(R.id.tv_search_group_name)
            TextView tv_search_group_name;
            @BindView(R.id.btn_search_group_action)
            ImageButton btn_search_group_action;

            SearchKeywordGroup searchKeywordGroup;

            KeywordGroupViewHolder(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.keyword_group_item, parent, false));
                ButterKnife.bind(this, this.itemView);

                btn_search_group_action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RxBus.getInstance().send(searchKeywordGroup);
                    }
                });
            }

            void bindTo(SearchKeywordGroup skg, int position) {
                this.searchKeywordGroup = skg;
                tv_search_group_name.setText(searchKeywordGroup.name);
                btn_search_group_action.setImageResource(searchKeywordGroup.iconResource);
                if (position == 0) {
                    v_divider.setVisibility(View.GONE);
                } else {
                    v_divider.setVisibility(View.VISIBLE);
                }
            }
        }

        static class KeywordViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_search_keyword)
            TextView tv_search_keyword;

            SearchKeyword searchKeyword;

            KeywordViewHolder(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.keyword_item, parent, false));
                ButterKnife.bind(this, this.itemView);
                tv_search_keyword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RxBus.getInstance().send(searchKeyword);
                    }
                });
            }

            void bindTo(SearchKeyword sk, boolean veryHot) {
                this.searchKeyword = sk;
                Context context = itemView.getContext();
                if (veryHot) {
                    tv_search_keyword.setText(new Spans()
                            .append(searchKeyword.bookName, new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimary)))
                            .append("*", new ImageSpan(context, R.drawable.ic_hot)));
                } else {
                    tv_search_keyword.setText(searchKeyword.bookName);
                }
            }
        }
    }
}

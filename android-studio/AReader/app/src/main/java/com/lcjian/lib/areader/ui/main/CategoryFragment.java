package com.lcjian.lib.areader.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lcjian.lib.areader.Global;
import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.data.entity.BookCategory;
import com.lcjian.lib.areader.data.entity.BookCategoryGroup;
import com.lcjian.lib.areader.data.entity.Displayable;
import com.lcjian.lib.areader.data.entity.PageResult;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.network.RestAPI;
import com.lcjian.lib.areader.ui.base.RecyclerFragment;
import com.lcjian.lib.areader.ui.category.CategoryBooksActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 分类列表
 *
 * @author LCJIAN
 */
public class CategoryFragment extends RecyclerFragment<Displayable> {

    private CategoryAdapter mAdapter;

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<Displayable> data) {
        mAdapter = new CategoryAdapter(data);
        return mAdapter;
    }

    @Override
    public Observable<PageResult<Displayable>> onCreatePageObservable(int currentPage) {
        return RestAPI.getInstance().readerService().categories()
                .map(new Function<ResponseData<Map<String, BookCategoryGroup>>, PageResult<Displayable>>() {
                    @Override
                    public PageResult<Displayable> apply(ResponseData<Map<String, BookCategoryGroup>> mapResponseData) {
                        PageResult<Displayable> result = new PageResult<>();
                        List<Displayable> elements = new ArrayList<>();

                        List<BookCategoryGroup> groups = new ArrayList<>(mapResponseData.data.values());
                        Collections.sort(groups, new Comparator<BookCategoryGroup>() {
                            @Override
                            public int compare(BookCategoryGroup o1, BookCategoryGroup o2) {
                                return (int) (o1.id - o2.id);
                            }
                        });

                        for (BookCategoryGroup group : groups) {
                            elements.add(group);
                            elements.addAll(group.categories);
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
                return mAdapter.getItemViewType(position) == CategoryAdapter.TYPE_CATEGORY_GROUP ? 2 : 1;
            }
        });
        recycler_view.setLayoutManager(gridLayoutManager);

        super.onViewCreated(view, savedInstanceState);
    }

    static class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        static final int TYPE_CATEGORY_GROUP = 0;
        static final int TYPE_CATEGORY = 1;

        private List<Displayable> mData;

        CategoryAdapter(List<Displayable> data) {
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
            return mData.get(position) instanceof BookCategoryGroup ? TYPE_CATEGORY_GROUP : TYPE_CATEGORY;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_CATEGORY_GROUP:
                    return new CategoryGroupViewHolder(parent);
                case TYPE_CATEGORY:
                    return new CategoryViewHolder(parent);
                default:
                    return new CategoryGroupViewHolder(parent);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof CategoryGroupViewHolder) {
                ((CategoryGroupViewHolder) holder).bindTo((BookCategoryGroup) mData.get(position));
            } else if (holder instanceof CategoryViewHolder) {
                ((CategoryViewHolder) holder).bindTo((BookCategory) mData.get(position));
            }
        }

        static class CategoryGroupViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_category_group_name)
            TextView tv_category_group_name;

            BookCategoryGroup categoryGroup;

            CategoryGroupViewHolder(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.category_group_item, parent, false));
                ButterKnife.bind(this, this.itemView);
            }

            void bindTo(BookCategoryGroup cg) {
                this.categoryGroup = cg;
                tv_category_group_name.setText(categoryGroup.name);
            }
        }

        static class CategoryViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.iv_cover_1)
            ImageView iv_cover_1;
            @BindView(R.id.iv_cover_2)
            ImageView iv_cover_2;
            @BindView(R.id.iv_cover_3)
            ImageView iv_cover_3;
            @BindView(R.id.tv_category_name)
            TextView tv_category_name;

            BookCategory category;

            CategoryViewHolder(ViewGroup parent) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false));
                ButterKnife.bind(this, this.itemView);
                this.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.getContext().startActivity(new Intent(v.getContext(), CategoryBooksActivity.class)
                                .putExtra("category", category));
                    }
                });
            }

            void bindTo(BookCategory c) {
                this.category = c;
                Glide.with(iv_cover_1)
                        .load(category.covers.get(0))
                        .apply(Global.roundedPoster)
                        .into(iv_cover_1);
                Glide.with(iv_cover_2)
                        .load(category.covers.get(1))
                        .apply(Global.roundedPoster)
                        .into(iv_cover_2);
                Glide.with(iv_cover_3)
                        .load(category.covers.get(2))
                        .apply(Global.roundedPoster)
                        .into(iv_cover_3);
                tv_category_name.setText(category.name);
            }
        }
    }
}

package com.lcjian.lib.areader.ui.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.lcjian.lib.areader.Global;
import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.RxBus;
import com.lcjian.lib.areader.data.entity.Book;
import com.lcjian.lib.areader.data.entity.BookGroup;
import com.lcjian.lib.areader.data.entity.Displayable;
import com.lcjian.lib.areader.data.entity.SlideBook;
import com.lcjian.lib.areader.ui.detail.BookDetailActivity;
import com.lcjian.lib.areader.widget.AutoViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 书城Adapter
 *
 * @author LCJIAN
 */
public class BookStoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SLIDE = 0;
    private static final int TYPE_BOOK_GROUP_HEAD = 1;
    private static final int TYPE_BOOK_LIST = 2;
    private static final int TYPE_BOOK_GRID = 3;
    private static final int TYPE_BOOK_GROUP_FOOT = 4;

    private List<Displayable> mData;

    BookStoreAdapter(List<Displayable> data) {
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

    List<Displayable> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position) instanceof BookGroup) {
            return TYPE_SLIDE;
        } else if (mData.get(position) instanceof BookGroup.GroupStartItem) {
            return TYPE_BOOK_GROUP_HEAD;
        } else if (mData.get(position) instanceof Book) {
            if (((Book) mData.get(position)).showMode == 0) {
                return TYPE_BOOK_LIST;
            } else {
                return TYPE_BOOK_GRID;
            }
        } else if (mData.get(position) instanceof BookGroup.GroupEndItem) {
            return TYPE_BOOK_GROUP_FOOT;
        } else {
            return TYPE_SLIDE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SLIDE:
                return new SlideViewHolder(parent);
            case TYPE_BOOK_GROUP_HEAD:
                return new GroupHeadViewHolder(parent);
            case TYPE_BOOK_LIST:
                return new BookViewHolder(parent);
            case TYPE_BOOK_GRID:
                return new BookGridViewHolder(parent);
            case TYPE_BOOK_GROUP_FOOT:
                return new GroupFootViewHolder(parent);
            default:
                return new BookViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SlideViewHolder) {
            ((SlideViewHolder) holder).bindTo((BookGroup) mData.get(position));
        } else if (holder instanceof GroupHeadViewHolder) {
            ((GroupHeadViewHolder) holder).bindTo((BookGroup.GroupStartItem) mData.get(position));
        } else if (holder instanceof BookViewHolder) {
            ((BookViewHolder) holder).bindTo((Book) mData.get(position));
        } else if (holder instanceof BookGridViewHolder) {
            ((BookGridViewHolder) holder).bindTo((Book) mData.get(position));
        } else if (holder instanceof GroupFootViewHolder) {
            ((GroupFootViewHolder) holder).bindTo((BookGroup.GroupEndItem) mData.get(position));
        }
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.vp_slide_book)
        AutoViewPager vp_slide_book;

        BookGroup bookGroup;

        SlideViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_book_item, parent, false));
            ButterKnife.bind(this, this.itemView);
        }

        void bindTo(BookGroup bg) {
            this.bookGroup = bg;
            vp_slide_book.setAdapter(new BannerAdapter(bookGroup.slide));
            vp_slide_book.setOffscreenPageLimit(2);
        }
    }

    static class GroupHeadViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_book_group_name)
        TextView tv_book_group_name;

        BookGroup.GroupStartItem groupStartItem;

        GroupHeadViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.book_group_head_item, parent, false));
            ButterKnife.bind(this, this.itemView);
        }

        void bindTo(BookGroup.GroupStartItem item) {
            this.groupStartItem = item;
            tv_book_group_name.setText(groupStartItem.name);
        }
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_book_poster)
        ImageView iv_book_poster;
        @BindView(R.id.tv_book_name)
        TextView tv_book_name;
        @BindView(R.id.tv_book_introduction)
        TextView tv_book_introduction;
        @BindView(R.id.tv_book_author)
        TextView tv_book_author;
        @BindView(R.id.tv_book_category)
        TextView tv_book_category;
        @BindView(R.id.tv_book_status)
        TextView tv_book_status;

        Book book;

        BookViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.book_list_item, parent, false));
            ButterKnife.bind(this, this.itemView);
            tv_book_introduction.setMaxLines(3);
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(), BookDetailActivity.class)
                            .putExtra("book_id", book.id));
                }
            });
        }

        void bindTo(Book b) {
            this.book = b;
            Glide.with(iv_book_poster)
                    .load(book.poster)
                    .apply(Global.roundedPoster2)
                    .into(iv_book_poster);
            tv_book_name.setText(book.name);
            tv_book_introduction.setText(book.introduction);
            tv_book_author.setText(book.author);
            tv_book_status.setText(book.status == 0 ? R.string.un_complete : R.string.complete);
            tv_book_category.setText(book.categoryName);
        }
    }

    static class BookGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_book_poster)
        ImageView iv_book_poster;
        @BindView(R.id.tv_book_name)
        TextView tv_book_name;
        @BindView(R.id.tv_book_author)
        TextView tv_book_author;

        Book book;

        BookGridViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.book_grid_item, parent, false));
            ButterKnife.bind(this, this.itemView);
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(), BookDetailActivity.class)
                            .putExtra("book_id", book.id));
                }
            });
        }

        void bindTo(Book b) {
            this.book = b;
            Glide.with(iv_book_poster)
                    .load(book.poster)
                    .apply(Global.roundedPoster2)
                    .into(iv_book_poster);
            tv_book_name.setText(book.name);
            tv_book_author.setText(book.author);
        }
    }

    static class GroupFootViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ll_view)
        LinearLayout ll_view;
        @BindView(R.id.tv_text)
        TextView tv_text;
        @BindView(R.id.iv_image)
        ImageView iv_image;

        BookGroup.GroupEndItem groupEndItem;

        GroupFootViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.book_group_foot_item, parent, false));
            ButterKnife.bind(this, this.itemView);
            ll_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RxBus.getInstance().send(groupEndItem);
                    iv_image.animate().rotationBy(360).setDuration(500).start();
                }
            });
        }

        void bindTo(BookGroup.GroupEndItem item) {
            this.groupEndItem = item;
            tv_text.setText(groupEndItem.name);
        }
    }

    static class BannerAdapter extends PagerAdapter {

        private List<SlideBook> mData;

        private List<View> mRecycledViews;

        BannerAdapter(List<SlideBook> data) {
            this.mData = data;
            this.mRecycledViews = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view;
            if (mRecycledViews.isEmpty()) {
                view = new ImageView(container.getContext());
                view.setLayoutParams(new ViewPager.LayoutParams());
                ((ImageView) view).setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                view = mRecycledViews.get(0);
                mRecycledViews.remove(0);
            }
            final SlideBook book = mData.get(position);

            Glide.with(container.getContext())
                    .load(book.image)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into((ImageView) view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(), BookDetailActivity.class)
                            .putExtra("book_id", book.id));
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
            mRecycledViews.add((View) object);
        }
    }
}

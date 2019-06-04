package com.lcjian.lib.areader.ui.main;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lcjian.lib.areader.Global;
import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.RxBus;
import com.lcjian.lib.areader.data.entity.Book;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 书架Adapter
 *
 * @author LCJIAN
 */
public class BookshelfAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Book> mData;

    private List<Book> mChecked;

    private GridLayoutManager mGridLayoutManager;

    private boolean mCheckMode;

    BookshelfAdapter(List<Book> data, GridLayoutManager gridLayoutManager) {
        this.mData = data;
        this.mChecked = new ArrayList<>();
        this.mGridLayoutManager = gridLayoutManager;
    }

    public void replaceAll(final List<Book> data) {
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

    public void setCheckMode(boolean checkMode) {
        this.mCheckMode = checkMode;

        notifyDataSetChanged();
    }

    public List<Book> getChecked() {
        return mChecked;
    }

    public List<Book> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return mGridLayoutManager.getSpanCount() == 1
                ? new BookshelfViewHolder(parent, mChecked, this)
                : new BookshelfGridViewHolder(parent, mChecked, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BookshelfViewHolder) {
            ((BookshelfViewHolder) holder).bindTo(mData.get(position), mCheckMode);
        } else if (holder instanceof BookshelfGridViewHolder) {
            ((BookshelfGridViewHolder) holder).bindTo(mData.get(position), mCheckMode);
        }
    }

    static class BookshelfViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_book_poster)
        ImageView iv_book_poster;
        @BindView(R.id.tv_book_name)
        TextView tv_book_name;
        @BindView(R.id.tv_book_latest_introduction)
        TextView tv_book_latest_introduction;
        @BindView(R.id.tv_book_read_info)
        TextView tv_book_read_info;
        @BindView(R.id.chb)
        CheckBox chb;

        Book book;

        boolean mCheckMode;

        List<Book> mChecked;

        BookshelfAdapter adapter;

        BookshelfViewHolder(ViewGroup parent, List<Book> checked, BookshelfAdapter adapter) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.book_shelf_list_item, parent, false));
            ButterKnife.bind(this, this.itemView);
            this.mChecked = checked;
            this.adapter = adapter;
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCheckMode) {
                        if (mChecked.contains(book)) {
                            mChecked.remove(book);
                        } else {
                            mChecked.add(book);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        RxBus.getInstance().send(book);
                    }
                }
            };
            this.chb.setOnClickListener(onClickListener);
            this.itemView.setOnClickListener(onClickListener);
            this.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    RxBus.getInstance().send(Boolean.TRUE);
                    return true;
                }
            });
        }

        void bindTo(Book b, boolean checkMode) {
            this.book = b;
            this.mCheckMode = checkMode;
            Glide.with(iv_book_poster)
                    .load(book.poster)
                    .apply(Global.roundedPoster2)
                    .into(iv_book_poster);
            tv_book_name.setText(book.name);
            tv_book_latest_introduction.setText(book.lastName);
            tv_book_read_info.setText(TextUtils.isEmpty(book.progress)
                    ? itemView.getContext().getString(R.string.unread)
                    : itemView.getContext().getString(R.string.read_progress, book.progress));
            chb.setVisibility(checkMode ? View.VISIBLE : View.INVISIBLE);
            chb.setChecked(mChecked.contains(book));
        }
    }

    static class BookshelfGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_book_poster)
        ImageView iv_book_poster;
        @BindView(R.id.tv_book_name)
        TextView tv_book_name;
        @BindView(R.id.tv_book_read_info)
        TextView tv_book_read_info;
        @BindView(R.id.chb)
        CheckBox chb;

        Book book;

        boolean mCheckMode;

        List<Book> mChecked;

        BookshelfAdapter adapter;

        BookshelfGridViewHolder(ViewGroup parent, List<Book> checked, BookshelfAdapter adapter) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.book_shelf_grid_item, parent, false));
            ButterKnife.bind(this, this.itemView);
            this.mChecked = checked;
            this.adapter = adapter;
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCheckMode) {
                        if (mChecked.contains(book)) {
                            mChecked.remove(book);
                        } else {
                            mChecked.add(book);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        RxBus.getInstance().send(book);
                    }
                }
            };
            this.chb.setOnClickListener(onClickListener);
            this.itemView.setOnClickListener(onClickListener);
            this.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    RxBus.getInstance().send(Boolean.TRUE);
                    return true;
                }
            });
        }

        void bindTo(Book b, boolean checkMode) {
            this.book = b;
            this.mCheckMode = checkMode;
            Glide.with(iv_book_poster)
                    .load(book.poster)
                    .apply(Global.roundedPoster2)
                    .into(iv_book_poster);
            tv_book_name.setText(book.name);
            tv_book_read_info.setText(TextUtils.isEmpty(book.progress)
                    ? itemView.getContext().getString(R.string.unread)
                    : itemView.getContext().getString(R.string.read_progress, book.progress));
            chb.setVisibility(checkMode ? View.VISIBLE : View.INVISIBLE);
            chb.setChecked(mChecked.contains(book));
        }
    }
}

package com.lcjian.lib.areader.ui.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lcjian.lib.areader.Global;
import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.data.entity.Book;
import com.lcjian.lib.areader.ui.detail.BookDetailActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 通用书籍列表Adapter
 *
 * @author LCJIAN
 */
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> mData;

    public BookAdapter(List<Book> data) {
        this.mData = data;
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

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @NonNull
    @Override
    public BookAdapter.BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookAdapter.BookViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull BookAdapter.BookViewHolder holder, int position) {
        holder.bindTo(mData.get(position));
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
}

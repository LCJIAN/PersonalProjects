package com.lcjian.lib.areader.ui.category;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.data.entity.BookCategory;
import com.lcjian.lib.areader.data.entity.BookCategoryChild;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.network.RestAPI;
import com.lcjian.lib.areader.ui.base.BaseActivity;
import com.lcjian.lib.areader.ui.main.MainActivity;
import com.yyydjk.library.DropDownMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 分类书籍列表
 *
 * @author LCJIAN
 */
public class CategoryBooksActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_right)
    TextView tv_right;
    @BindView(R.id.drop_down_menu)
    DropDownMenu drop_down_menu;

    private View contentView;

    private BookCategory mCategory;
    private List<BookCategoryChild> mChildCategories;

    private String headers[] = {"类型", "状态"};
    private List<View> popupViews = new ArrayList<>();
    private BookCategoryChild mCurrentChildCategory;
    private int mCurrentStatus; // （-1:全部 0:连载 1：完结）

    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategory = (BookCategory) getIntent().getSerializableExtra("category");
        setContentView(R.layout.activity_category_books);
        ButterKnife.bind(this);

        btn_back.setVisibility(View.VISIBLE);
        tv_right.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(this);
        tv_right.setOnClickListener(this);
        tv_right.setText(R.string.bookshelf);
        tv_title.setText(mCategory.name);
        contentView = LayoutInflater.from(this).inflate(R.layout.content_container, drop_down_menu, false);

        mDisposable = RestAPI.getInstance().readerService()
                .childCategories(mCategory.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseData<List<BookCategoryChild>>>() {
                    @Override
                    public void accept(ResponseData<List<BookCategoryChild>> listResponseData) {
                        mChildCategories = listResponseData.data;
                        BookCategoryChild bcc = new BookCategoryChild();
                        bcc.name = getString(R.string.all);
                        bcc.id = mCategory.id;
                        mChildCategories.add(0, bcc);
                        final List<String> childCategories = new ArrayList<>();
                        for (BookCategoryChild childCategory : mChildCategories) {
                            childCategories.add(childCategory.name);
                        }
                        final List<String> status = Arrays.asList(
                                getString(R.string.all),
                                getString(R.string.un_complete),
                                getString(R.string.complete));

                        ListView lv_category = new ListView(CategoryBooksActivity.this);
                        lv_category.setDivider(new ColorDrawable(0xfff0f0f0));
                        lv_category.setDividerHeight(1);
                        lv_category.setAdapter(new ListDropDownAdapter(CategoryBooksActivity.this, childCategories));
                        lv_category.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ((ListDropDownAdapter) parent.getAdapter()).setCheckItem(position);
                                drop_down_menu.setTabText(position == 0 ? headers[0] : childCategories.get(position));
                                drop_down_menu.closeMenu();

                                mCurrentChildCategory = mChildCategories.get(position);
                                refresh();
                            }
                        });
                        popupViews.add(lv_category);

                        ListView lv_status = new ListView(CategoryBooksActivity.this);
                        lv_status.setDivider(new ColorDrawable(0xfff0f0f0));
                        lv_status.setDividerHeight(1);
                        lv_status.setAdapter(new ListDropDownAdapter(CategoryBooksActivity.this, status));
                        lv_status.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ((ListDropDownAdapter) parent.getAdapter()).setCheckItem(position);
                                drop_down_menu.setTabText(position == 0 ? headers[1] : status.get(position));
                                drop_down_menu.closeMenu();

                                mCurrentStatus = position - 1;
                                refresh();
                            }
                        });
                        popupViews.add(lv_status);
                        drop_down_menu.setDropDownMenu(Arrays.asList(headers), popupViews, contentView);

                        mCurrentChildCategory = mChildCategories.get(0);
                        mCurrentStatus = -1;
                        refresh();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                });
    }

    private void refresh() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container,
                CategoryBooksFragment.newInstance(mCurrentChildCategory.id, mCurrentStatus)).commit();
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.tv_right:
                startActivity(new Intent(v.getContext(), MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            default:
                break;
        }
    }
}

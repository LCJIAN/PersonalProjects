package com.lcjian.lib.areader.ui.main;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.RxBus;
import com.lcjian.lib.areader.ui.base.BaseActivity;
import com.lcjian.lib.areader.ui.search.SearchActivity;
import com.lcjian.lib.areader.util.FragmentSwitchHelper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_right)
    ImageButton btn_right;
    @BindView(R.id.rg_bottom_navigation)
    RadioGroup rg_bottom_navigation;
    @BindView(R.id.ll_edit_shelf)
    LinearLayout ll_edit_shelf;
    @BindView(R.id.tv_select_all)
    TextView tv_select_all;
    @BindView(R.id.tv_delete)
    TextView tv_delete;

    private FragmentSwitchHelper mFragmentSwitchHelper;

    private CompositeDisposable mDisposables;
    private Disposable mDisposable;

    private int mCheckedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        rg_bottom_navigation.setOnCheckedChangeListener(this);
        mFragmentSwitchHelper = FragmentSwitchHelper.create(
                R.id.fl_main_fragment_container,
                getSupportFragmentManager(),
                true,
                new BookshelfFragment(),
                new BookStoreFragment(),
                new CategoryFragment(),
                new RankFragment());

        btn_right.setVisibility(View.VISIBLE);
        btn_right.setOnClickListener(this);
        tv_select_all.setOnClickListener(this);
        tv_delete.setOnClickListener(this);

        mDisposable = new RxPermissions(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) {
                        if (!granted) {
                            finish();
                        } else {
                            rg_bottom_navigation.check(R.id.rb_book_store);
                        }
                    }
                });
        mDisposables = new CompositeDisposable();
        mDisposables.add(RxBus.getInstance()
                .asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object event) {
                        if (event instanceof Boolean) {
                            TransitionManager.beginDelayedTransition((ViewGroup) ll_edit_shelf.getParent());
                            ll_edit_shelf.setVisibility(((Boolean) event) ? View.VISIBLE : View.INVISIBLE);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                }));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (mCheckedId == checkedId) {
            return;
        }
        switch (checkedId) {
            case R.id.rb_bookshelf: {
                mFragmentSwitchHelper.changeFragment(BookshelfFragment.class);
                tv_title.setText(R.string.bookshelf);
                btn_right.setVisibility(View.VISIBLE);
            }
            break;
            case R.id.rb_book_store: {
                mFragmentSwitchHelper.changeFragment(BookStoreFragment.class);
                tv_title.setText(R.string.book_store);
                btn_right.setVisibility(View.VISIBLE);
            }
            break;
            case R.id.rb_book_category: {
                mFragmentSwitchHelper.changeFragment(CategoryFragment.class);
                tv_title.setText(R.string.book_category);
                btn_right.setVisibility(View.GONE);
            }
            break;
            case R.id.rb_book_rank: {
                mFragmentSwitchHelper.changeFragment(RankFragment.class);
                tv_title.setText(R.string.book_rank);
                btn_right.setVisibility(View.GONE);
            }
            break;
            default:
                break;
        }
        mCheckedId = checkedId;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                startActivity(new Intent(v.getContext(), SearchActivity.class));
                break;
            case R.id.tv_select_all:
                RxBus.getInstance().send("select_all_bookshelf");
                break;
            case R.id.tv_delete:
                RxBus.getInstance().send("delete_bookshelf");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        mDisposables.dispose();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        rg_bottom_navigation.check(R.id.rb_bookshelf);
    }

    @Override
    public void onBackPressed() {
        if (ll_edit_shelf.getVisibility() == View.VISIBLE) {
            TransitionManager.beginDelayedTransition((ViewGroup) ll_edit_shelf.getParent());
            ll_edit_shelf.setVisibility(View.INVISIBLE);
            RxBus.getInstance().send("clean_bookshelf_check");
            RxBus.getInstance().send(false);
        } else {
            super.onBackPressed();
        }
    }

    public void check(@IdRes int id) {
        rg_bottom_navigation.check(id);
    }
}

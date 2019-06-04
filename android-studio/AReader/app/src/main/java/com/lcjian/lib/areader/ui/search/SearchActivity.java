package com.lcjian.lib.areader.ui.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.lib.areader.App;
import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.RxBus;
import com.lcjian.lib.areader.data.entity.SearchKeyword;
import com.lcjian.lib.areader.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

/**
 * 搜索
 *
 * @author LCJIAN
 */
public class SearchActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener, TextWatcher {

    @BindView(R.id.fl_keyword)
    FrameLayout fl_keyword;
    @BindView(R.id.et_keyword)
    EditText et_keyword;
    @BindView(R.id.btn_clear)
    ImageButton btn_clear;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;

    private CompositeDisposable mDisposables;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        btn_clear.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        et_keyword.setOnEditorActionListener(this);
        et_keyword.addTextChangedListener(this);

        mDisposables = new CompositeDisposable();
        mDisposables.add(RxBus.getInstance().asFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object event) {
                        if (event instanceof SearchKeyword) {
                            String keyword = ((SearchKeyword) event).bookName.trim();

                            SharedPreferences sp = App.getInstance().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                            List<String> strings = new ArrayList<>(Arrays.asList(sp.getString("search_history", "").split(",")));
                            if (strings.contains(keyword)) {
                                strings.remove(keyword);
                            }
                            strings.add(0, keyword);
                            sp.edit().putString("search_history", TextUtils.join(",", strings)).apply();

                            et_keyword.setText(keyword);
                            removeSearchResult();
                            getSupportFragmentManager().beginTransaction().add(R.id.fl_fragment_container,
                                    SearchResultFragment.newInstance(keyword), "SearchResultFragment").commit();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                }));

        getSupportFragmentManager().beginTransaction().add(R.id.fl_fragment_container,
                new SearchKeywordFragment(), "SearchKeywordFragment").commit();
    }

    @Override
    protected void onDestroy() {
        mDisposables.dispose();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear: {
                et_keyword.getEditableText().clear();
            }
            break;
            case R.id.tv_cancel:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!removeSearchResult()) {
            super.onBackPressed();
        }
    }

    private boolean removeSearchResult() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment searchResultFragment = fragmentManager.findFragmentByTag("SearchResultFragment");
        if (searchResultFragment != null) {
            fragmentManager.beginTransaction().remove(searchResultFragment).commit();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            SearchKeyword searchKeyword = new SearchKeyword();
            searchKeyword.bookName = et_keyword.getEditableText().toString();
            RxBus.getInstance().send(searchKeyword);
            return true;
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        TransitionManager.beginDelayedTransition(fl_keyword);
        if (TextUtils.isEmpty(s)) {
            btn_clear.setVisibility(View.GONE);
        } else {
            btn_clear.setVisibility(View.VISIBLE);
        }
    }
}

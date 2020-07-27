package com.org.firefighting.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.tabs.TabLayout;
import com.lcjian.lib.content.SimpleFragmentPagerAdapter;
import com.lcjian.lib.recyclerview.EmptyAdapter;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.util.common.SoftKeyboardUtils;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.data.entity.PageResult;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.SearchRequest;
import com.org.firefighting.data.network.entity.SearchResult;
import com.org.firefighting.data.network.entity.ServiceEntity;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.base.RecyclerFragment;
import com.org.firefighting.ui.resource.ResourceDetailActivity;
import com.org.firefighting.ui.service.ServiceDataQueryActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.et_keyword)
    EditText et_keyword;
    @BindView(R.id.btn_search)
    TextView btn_search;
    @BindView(R.id.tab_search)
    TabLayout tab_search;
    @BindView(R.id.vp_search)
    ViewPager vp_search;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        btn_back.setOnClickListener(v -> onBackPressed());
        btn_search.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(et_keyword.getEditableText())) {
                RxBus.getInstance().send(et_keyword.getEditableText());
            }
            SoftKeyboardUtils.hideSoftInput(v.getContext());
        });
        et_keyword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!TextUtils.isEmpty(et_keyword.getEditableText())) {
                    RxBus.getInstance().send(et_keyword.getEditableText());
                }
                SoftKeyboardUtils.hideSoftInput(v.getContext());
            }
            return false;
        });

        vp_search.setOffscreenPageLimit(4);
        vp_search.setAdapter(new SimpleFragmentPagerAdapter(getSupportFragmentManager())
                .addFragment(SearchFragment.newInstance("100000"), "资源目录")
                .addFragment(SearchFragment.newInstance("110000"), "服务")
                .addFragment(SearchFragment.newInstance("170010"), "文档")
                .addFragment(SearchFragment.newInstance("170020"), "标准"));
        tab_search.setupWithViewPager(vp_search);
    }

    public static class SearchFragment extends RecyclerFragment<SearchResult> {

        private String mCategoryId;
        private String mKeyword;

        private View mEmptyView;
        private SlimAdapter mAdapter;

        private Disposable mDisposable;

        private static SearchFragment newInstance(String categoryId) {
            SearchFragment fragment = new SearchFragment();
            Bundle args = new Bundle();
            args.putString("category_id", categoryId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mCategoryId = getArguments().getString("category_id");
            }
        }

        @Override
        protected void onEmptyAdapterCreated(EmptyAdapter emptyAdapter) {
            mEmptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_data, recycler_view, false);
            emptyAdapter.setEmptyView(mEmptyView);
        }

        @Override
        protected void onEmptyViewShow(boolean error) {
            ((ImageView) mEmptyView).setImageResource(error ? R.drawable.net_error : R.drawable.no_search_result);
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<SearchResult> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<SearchResult>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.search_result_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<SearchResult> viewHolder) {
                            viewHolder.clicked(v -> {
                                if (TextUtils.equals("100000", mCategoryId)) {
                                    startActivity(new Intent(v.getContext(), ResourceDetailActivity.class)
                                            .putExtra("resource_id", viewHolder.itemData.id)
                                            .putExtra("resource_table_comment", viewHolder.itemData.title));
                                } else if (TextUtils.equals("110000", mCategoryId)) {
                                    ServiceEntity serviceEntity = new ServiceEntity();
                                    serviceEntity.id = viewHolder.itemData.id;
                                    serviceEntity.serviceName = viewHolder.itemData.title;
                                    serviceEntity.invokeName = viewHolder.itemData.id;
                                    serviceEntity.applyStatus = 0;
                                    startActivity(new Intent(v.getContext(), ServiceDataQueryActivity.class)
                                            .putExtra("service", serviceEntity));
                                }
                            });
                        }

                        @Override
                        public void onBind(SearchResult data, SlimAdapter.SlimViewHolder<SearchResult> viewHolder) {
                            FlexboxLayout ll_label = viewHolder.findViewById(R.id.ll_label);
                            View view = ll_label.getChildAt(0);
                            ll_label.removeAllViews();
                            ll_label.addView(view);
                            if (!TextUtils.isEmpty(data.label)) {
                                String[] labels = data.label.trim().split(" ");
                                LayoutInflater inflater = LayoutInflater.from(ll_label.getContext());
                                for (String label : labels) {
                                    TextView tv = (TextView) inflater.inflate(R.layout.search_result_label_item, ll_label, false);
                                    tv.setText(label);
                                    ll_label.addView(tv);
                                }
                            }

                            viewHolder
                                    .text(R.id.tv_title, Html.fromHtml(data.title))
                                    .text(R.id.tv_content, Html.fromHtml(data.content))
                                    .text(R.id.tv_keyword, "关键字:" + data.keywords);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<SearchResult>> onCreatePageObservable(int currentPage) {
            if (TextUtils.isEmpty(mKeyword)) {
                return null;
            }
            SearchRequest sr = new SearchRequest();
            sr.page = currentPage;
            sr.pager = 20;
            sr.category = mCategoryId;
            sr.search = mKeyword;
            return RestAPI.getInstance().apiServiceSB().search(sr)
                    .map(responseData -> {
                        if (responseData.total == null) {
                            responseData.total = 0;
                        }
                        PageResult<SearchResult> pageResult = new PageResult<>();
                        pageResult.elements = responseData.result;
                        pageResult.page_number = currentPage;
                        pageResult.page_size = 20;
                        pageResult.total_pages = responseData.total % 20 == 0
                                ? responseData.total / 20
                                : responseData.total / 20 + 1;
                        pageResult.total_elements = responseData.total;
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<SearchResult> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            super.onViewCreated(view, savedInstanceState);

            mDisposable = RxBus.getInstance().asFlowable()
                    .filter(o -> o instanceof Editable)
                    .debounce(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                        mKeyword = o.toString();
                        refresh();
                    });
        }

        @Override
        public void onDestroyView() {
            mDisposable.dispose();
            super.onDestroyView();
        }
    }
}

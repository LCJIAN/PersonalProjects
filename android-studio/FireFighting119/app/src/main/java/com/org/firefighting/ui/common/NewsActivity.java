package com.org.firefighting.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lcjian.lib.recyclerview.EmptyAdapter;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.org.firefighting.BuildConfig;
import com.org.firefighting.R;
import com.org.firefighting.data.entity.PageResult;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.News;
import com.org.firefighting.data.network.entity.NewsCategory;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.base.RecyclerFragment;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class NewsActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tab_news)
    TabLayout tab_news;
    @BindView(R.id.vp_news)
    ViewPager2 vp_news;

    private CategoryPagerAdapter mAdapter;

    private TabLayoutMediator mTabLayoutMediator;

    private List<NewsCategory> mCategories;

    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        ButterKnife.bind(this);

        btn_back.setOnClickListener(v -> onBackPressed());
        tv_title.setText(R.string.helping);

        mAdapter = new CategoryPagerAdapter(NewsActivity.this);
        vp_news.setAdapter(mAdapter);
        mTabLayoutMediator = new TabLayoutMediator(tab_news, vp_news, (tab, position) -> tab.setText(mCategories.get(position).websiteName));
        mTabLayoutMediator.attach();

        getCategories();
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        mTabLayoutMediator.detach();
        super.onDestroy();
    }

    private void getCategories() {
        showProgress();
        mDisposable = RestAPI.getInstance().apiServiceSB().getNewsCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pageResponse -> {
                            hideProgress();
                            Collections.reverse(pageResponse.result);
                            mCategories = pageResponse.result;
                            mAdapter.setData(mCategories);
                            mAdapter.notifyDataSetChanged();
                        },
                        throwable -> {
                            hideProgress();
                            Timber.w(throwable);
                        });
    }

    private static class CategoryPagerAdapter extends FragmentStateAdapter {

        private List<NewsCategory> mData;

        public CategoryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return NewsFragment.newInstance(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public long getItemId(int position) {
            return mData.get(position).hashCode();
        }

        @Override
        public boolean containsItem(long itemId) {
            for (NewsCategory c : mData) {
                if (c.hashCode() == itemId) {
                    return true;
                }
            }
            return false;
        }

        public void setData(List<NewsCategory> data) {
            this.mData = data;
        }
    }

    public static class NewsFragment extends RecyclerFragment<News> {

        private View mEmptyView;
        private SlimAdapter mAdapter;

        private NewsCategory mCategory;

        public static NewsFragment newInstance(NewsCategory category) {
            NewsFragment fragment = new NewsFragment();
            Bundle args = new Bundle();
            args.putSerializable("news_category", category);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mCategory = (NewsCategory) getArguments().getSerializable("news_category");
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
        public RecyclerView.Adapter onCreateAdapter(List<News> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<News>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.news_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<News> viewHolder) {
                            viewHolder.clicked(v -> startActivity(new Intent(v.getContext(), WebViewActivity.class)
                                    .putExtra("url", BuildConfig.API_URL_SB + "crawler" + viewHolder.itemData.filePath + "/" + viewHolder.itemData.fileName)));
                        }

                        @Override
                        public void onBind(News data, SlimAdapter.SlimViewHolder<News> viewHolder) {
                            viewHolder
                                    .background(R.id.cl_news,
                                            viewHolder.getAbsoluteAdapterPosition() == 0 ? R.drawable.shape_card_top :
                                                    (viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1 ? R.drawable.shape_card_bottom :
                                                            R.drawable.shape_card_middle))
                                    .text(R.id.tv_news, data.title)
                                    .text(R.id.tv_news_from, data.sourceName)
                                    .text(R.id.tv_news_time, data.createDate);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<News>> onCreatePageObservable(int currentPage) {
            return RestAPI.getInstance().apiServiceSB()
                    .getNews(mCategory.websiteUrlMd5, currentPage, 20)
                    .map(responseData -> {
                        PageResult<News> pageResult = new PageResult<>();
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
                    .observeOn(AndroidSchedulers.mainThread())
                    .cache();
        }

        @Override
        public void notifyDataChanged(List<News> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            super.onViewCreated(view, savedInstanceState);
        }
    }
}

package com.org.firefighting.ui.resource;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.lcjian.lib.recyclerview.EmptyAdapter;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.text.Spans;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.data.entity.PageResult;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.base.RecyclerFragment;
import com.org.firefighting.ui.common.SearchActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ResourcesActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.btn_go_search)
    ImageButton btn_go_search;
    @BindView(R.id.tab_resource)
    TabLayout tab_resource;
    @BindView(R.id.vp_resource)
    ViewPager vp_resource;

    private List<String> mTitlesO = Arrays.asList("所有", "已申请", "已收藏");
    private List<String> mTitles = new ArrayList<>(mTitlesO);

    private ResourcePagerAdapter mPagerAdapter;

    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);
        ButterKnife.bind(this);

        btn_back.setOnClickListener(v -> onBackPressed());
        btn_go_search.setOnClickListener(v -> startActivity(new Intent(v.getContext(), SearchActivity.class)));

        mPagerAdapter = new ResourcePagerAdapter(getSupportFragmentManager(), mTitles);

        vp_resource.setOffscreenPageLimit(3);
        vp_resource.setAdapter(mPagerAdapter);
        tab_resource.setupWithViewPager(vp_resource);

        mDisposable = RxBus.getInstance().asFlowable()
                .filter(o -> o instanceof TitleChangeEvent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    TitleChangeEvent e = (TitleChangeEvent) o;
                    for (int i = 0; i < mTitlesO.size(); i++) {
                        if (TextUtils.equals(mTitlesO.get(i), e.title)) {
                            mTitles.set(i, e.title + e.count);
                        }
                    }
                    mPagerAdapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }

    private static class ResourcePagerAdapter extends FragmentStatePagerAdapter {

        private List<String> mTitles;

        private ResourcePagerAdapter(FragmentManager fm, List<String> titles) {
            super(fm);
            this.mTitles = titles;
        }

        @Override
        @NonNull
        public Fragment getItem(int position) {
            return ResourcesFragment.newInstance(mTitles.get(position));
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }

        @Override
        public int getCount() {
            return mTitles == null ? 0 : mTitles.size();
        }
    }

    private static class TitleChangeEvent {

        private String title;
        private int count;

        private TitleChangeEvent(String title, int count) {
            this.title = title;
            this.count = count;
        }
    }

    public static class ResourcesFragment extends RecyclerFragment<ResourceEntity> {

        private View mEmptyView;
        private SlimAdapter mAdapter;

        private String mTabTitle;

        private static ResourcesFragment newInstance(String tabTitle) {
            ResourcesFragment fragment = new ResourcesFragment();
            fragment.mTabTitle = tabTitle;
            return fragment;
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
        public RecyclerView.Adapter onCreateAdapter(List<ResourceEntity> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<ResourceEntity>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.resource_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<ResourceEntity> viewHolder) {
                            viewHolder.clicked(v -> startActivity(new Intent(v.getContext(), ResourceDetailActivity.class)
                                    .putExtra("resource_id", viewHolder.itemData.id)
                                    .putExtra("resource_table_comment", viewHolder.itemData.tableComment)));
                        }

                        @Override
                        public void onBind(ResourceEntity data, SlimAdapter.SlimViewHolder<ResourceEntity> viewHolder) {
                            viewHolder.text(R.id.tv_identity, "标识符：" + data.shareXxzybh)
                                    .text(R.id.tv_address_info, data.shareXxzymc)
                                    .text(R.id.tv_police_category, "所属业务警种：" + data.shareXxzyflSsywjz)
                                    .text(R.id.tv_factor, "所属要素：" + data.shareXxzyflSsys)
                                    .text(R.id.tv_content, data.tableComment)
                                    .text(R.id.tv_supplier, "提供单位：" + data.unitName)
                                    .text(R.id.tv_count_1, new Spans("浏览：").append(String.valueOf(data.browses), new ForegroundColorSpan(Color.RED)))
                                    .text(R.id.tv_count_2, new Spans("调用：").append(String.valueOf(data.calls), new ForegroundColorSpan(Color.RED)))
                                    .text(R.id.tv_count_3, new Spans("下载：").append(String.valueOf(data.download), new ForegroundColorSpan(Color.RED)))
                                    .text(R.id.tv_count_4, new Spans("调阅：").append(String.valueOf(data.apply), new ForegroundColorSpan(Color.RED)));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<ResourceEntity>> onCreatePageObservable(int currentPage) {
            return RestAPI.getInstance().apiServiceSB2()
                    .getResources(null, SharedPreferencesDataSource.getSignInResponse().user.id,
                            null, null, currentPage, 1000)
                    .map(responseData -> {
                        PageResult<ResourceEntity> pageResult = new PageResult<>();

                        List<ResourceEntity> result = new ArrayList<>();
                        if (TextUtils.equals("已申请", mTabTitle)) {
                            for (ResourceEntity resourceEntity : responseData.data.result) {
                                if (TextUtils.equals("1", resourceEntity.applyStatus) // 已申请
                                        || TextUtils.equals("2", resourceEntity.applyStatus)) { // 审核通过
                                    result.add(resourceEntity);
                                }
                            }
                        } else if (TextUtils.equals("已收藏", mTabTitle)) {
                            for (ResourceEntity resourceEntity : responseData.data.result) {
                                if (resourceEntity.collectStatus == 1) {
                                    result.add(resourceEntity);
                                }
                            }
                        } else { // 全部
                            result.addAll(responseData.data.result);
                        }

                        pageResult.elements = result;
                        pageResult.page_number = currentPage;
                        pageResult.page_size = 1000;
                        pageResult.total_pages = result.size() % 1000 == 0
                                ? result.size() / 1000
                                : result.size() / 1000 + 1;
                        pageResult.total_elements = result.size();
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<ResourceEntity> data) {
            RxBus.getInstance().send(new TitleChangeEvent(mTabTitle, data.size()));
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

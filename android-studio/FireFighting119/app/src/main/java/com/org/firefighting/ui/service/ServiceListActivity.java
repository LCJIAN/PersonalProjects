package com.org.firefighting.ui.service;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
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
import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.common.DimenUtils;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.data.entity.PageResult;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.ServiceEntity;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.base.RecyclerFragment;
import com.org.firefighting.ui.common.SearchActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.nekocode.badge.BadgeDrawable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ServiceListActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.btn_go_search)
    ImageButton btn_go_search;
    @BindView(R.id.tab_service)
    TabLayout tab_service;
    @BindView(R.id.vp_service)
    ViewPager2 vp_service;

    private TabLayoutMediator mTabLayoutMediator;
    private ServicePagerAdapter mPagerAdapter;

    private List<String> mTitlesO = Arrays.asList("所有", "已申请", "已收藏");
    private List<Pair<String, Integer>> mPairs = new ArrayList<>();

    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        ButterKnife.bind(this);

        btn_back.setOnClickListener(v -> onBackPressed());
        btn_go_search.setOnClickListener(v -> startActivity(new Intent(v.getContext(), SearchActivity.class)));

        mPagerAdapter = new ServicePagerAdapter(this);
        for (String string : mTitlesO) {
            mPairs.add(Pair.create(string, 0));
        }
        mPagerAdapter.setData(mTitlesO);
        vp_service.setOffscreenPageLimit(3);
        vp_service.setAdapter(mPagerAdapter);
        mTabLayoutMediator = new TabLayoutMediator(tab_service, vp_service, (tab, position) -> {
            Pair<String, Integer> pair = mPairs.get(position);
            tab.setText(pair.first + "[" + pair.second + "]");
        });
        mTabLayoutMediator.attach();

        mDisposable = RxBus.getInstance().asFlowable()
                .filter(o -> o instanceof TitleChangeEvent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    TitleChangeEvent e = (TitleChangeEvent) o;
                    for (int i = 0; i < mTitlesO.size(); i++) {
                        if (TextUtils.equals(mTitlesO.get(i), e.title)) {
                            mPairs.set(i, Pair.create(e.title, e.count));
                        }
                    }
                    mPagerAdapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        mTabLayoutMediator.detach();
        mDisposable.dispose();
        super.onDestroy();
    }

    private static class ServicePagerAdapter extends FragmentStateAdapter {

        private List<String> mData;

        public ServicePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return ServiceListFragment.newInstance(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public long getItemId(int position) {
            String name = mData.get(position);
            assert name != null;
            return name.hashCode();
        }

        @Override
        public boolean containsItem(long itemId) {
            for (String name : mData) {
                assert name != null;
                if (name.hashCode() == itemId) {
                    return true;
                }
            }
            return false;
        }

        public void setData(List<String> data) {
            this.mData = data;
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

    public static class ServiceListFragment extends RecyclerFragment<ServiceEntity> {

        private View mEmptyView;
        private SlimAdapter mAdapter;

        private String mTabTitle;

        private Disposable mDisposableR;
        private boolean mNeedRefresh;

        private static ServiceListFragment newInstance(String tabTitle) {
            ServiceListFragment fragment = new ServiceListFragment();
            fragment.mTabTitle = tabTitle;
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mDisposableR = RxBus.getInstance()
                    .asFlowable()
                    .filter(o -> o instanceof RefreshServicesEvent)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> mNeedRefresh = true);
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
        public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> onCreateAdapter(List<ServiceEntity> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<ServiceEntity>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.resource_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<ServiceEntity> viewHolder) {
                            viewHolder.clicked(v -> startActivity(new Intent(v.getContext(), ServiceDetailActivity.class)
                                    .putExtra("service_id", viewHolder.itemData.id)));
                        }

                        @Override
                        public void onBind(ServiceEntity data, SlimAdapter.SlimViewHolder<ServiceEntity> viewHolder) {
                            String applyStr;
                            int color;
                            boolean visible;
                            if (1 == data.applyStatus) {
                                applyStr = "待审核";
                                color = 0xffdf8b07;
                                visible = true;
                            } else if (2 == data.applyStatus) {
                                applyStr = "审核通过";
                                color = 0xff1eb01b;
                                visible = true;
                            } else if (3 == data.applyStatus) {
                                applyStr = "审核未通过!";
                                color = 0xffd23319;
                                visible = true;
                            } else {
                                applyStr = "暂无使用权限";
                                color = 0xffd23319;
                                visible = false;
                            }
                            Spans spans = new Spans().append(data.serviceName);
                            if (visible) {
                                spans.append(" ").append(new BadgeDrawable.Builder()
                                        .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                                        .badgeColor(color)
                                        .typeFace(Typeface.DEFAULT)
                                        .textSize(DimenUtils.spToPixels(10, viewHolder.itemView.getContext()))
                                        .textColor(0xffffffff)
                                        .text1(" " + applyStr + " ")
                                        .build()
                                        .toSpannable());
                            }
                            viewHolder
                                    .text(R.id.tv_content, spans)
                                    .with(R.id.iv_favourite, view -> ((ImageView) view).setImageResource(data.collectStatus == 0 ? R.drawable.ic_favourite_not : R.drawable.ic_favourite))
                                    .text(R.id.tv_share_method, "接口类型：" + (TextUtils.isEmpty(data.type) ? "未知" : data.type))
                                    .text(R.id.tv_supplier, "管理单位：" + (TextUtils.isEmpty(data.serviceDeveloper) ? "暂无" : data.serviceDeveloper))
                                    .text(R.id.tv_publish_time, "发布时间：" + data.createDate)
                                    .text(R.id.tv_count_1, new Spans().append(String.valueOf(data.browses)));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<ServiceEntity>> onCreatePageObservable(int currentPage) {
            return Single.just(Pair.create(mTabTitle, currentPage))
                    .flatMap(pair -> {
                        String s = pair.first;
                        Integer p = pair.second;
                        if (TextUtils.equals("已申请", s)) {
                            return RestAPI.getInstance().apiServiceSB()
                                    .getServicesA(SharedPreferencesDataSource.getSignInResponse().user.id, p, 20);
                        } else if (TextUtils.equals("已收藏", s)) {
                            return RestAPI.getInstance().apiServiceSB()
                                    .getServicesF(SharedPreferencesDataSource.getSignInResponse().user.id, p, 20);
                        } else {
                            return RestAPI.getInstance().apiServiceSB()
                                    .getServices("61", null, SharedPreferencesDataSource.getSignInResponse().user.id,
                                            null, p, 20);
                        }
                    })
                    .map(responseData -> {
                        PageResult<ServiceEntity> pageResult = new PageResult<>();
                        pageResult.elements = responseData.data.result;
                        pageResult.page_number = currentPage;
                        pageResult.page_size = 20;
                        pageResult.total_pages = responseData.data.total % 20 == 0
                                ? responseData.data.total / 20
                                : responseData.data.total / 20 + 1;
                        pageResult.total_elements = responseData.data.total;
                        return pageResult;
                    })
                    .toObservable()
                    .doOnNext(result -> RxBus.getInstance().send(new TitleChangeEvent(mTabTitle, result.total_elements)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<ServiceEntity> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            super.onViewCreated(view, savedInstanceState);
        }

        @Override
        public void onResume() {
            if (mNeedRefresh) {
                refresh();
                mNeedRefresh = false;
            }
            super.onResume();
        }

        @Override
        public void onDestroy() {
            mDisposableR.dispose();
            super.onDestroy();
        }
    }

    static class RefreshServicesEvent {
    }
}

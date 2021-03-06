package com.org.firefighting.ui.resource;

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
import com.lcjian.lib.util.Triple;
import com.lcjian.lib.util.common.DimenUtils;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.data.entity.PageResult;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.Dir;
import com.org.firefighting.data.network.entity.DirRoot;
import com.org.firefighting.data.network.entity.ResourceEntity;
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

public class ResourcesActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.btn_go_search)
    ImageButton btn_go_search;
    @BindView(R.id.btn_dir)
    ImageButton btn_dir;
    @BindView(R.id.tab_resource)
    TabLayout tab_resource;
    @BindView(R.id.vp_resource)
    ViewPager2 vp_resource;

    private TabLayoutMediator mTabLayoutMediator;
    private ResourcePagerAdapter mPagerAdapter;

    private List<String> mTitlesO = Arrays.asList("所有", "已申请", "已收藏");
    private List<Triple<String, String, Integer>> mTriples = new ArrayList<>();

    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);
        ButterKnife.bind(this);

        btn_back.setOnClickListener(v -> onBackPressed());
        btn_go_search.setOnClickListener(v -> startActivity(new Intent(v.getContext(), SearchActivity.class)));
        btn_dir.setOnClickListener(v -> {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("TabPopFragment");
            if (fragment == null) {
                TabPopFragment tabPopFragment = new TabPopFragment()
                        .setListener(new TabPopFragment.Listener() {

                            @Override
                            public void onTabClicked(DirRoot dirRoot) {
                                mTitlesO.set(0, dirRoot.label);
                                mTriples.set(0, Triple.create(dirRoot.label, dirRoot.value, mTriples.get(0).third));
                                mPagerAdapter.notifyDataSetChanged();
                                vp_resource.setCurrentItem(0);
                            }

                            @Override
                            public void onTabClicked(Dir dir) {
                                mTitlesO.set(0, dir.name);
                                mTriples.set(0, Triple.create(dir.name, dir.dirCode, mTriples.get(0).third));
                                mPagerAdapter.notifyDataSetChanged();
                                vp_resource.setCurrentItem(0);
                            }
                        });
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_fragment_container, tabPopFragment, "TabPopFragment")
                        .addToBackStack("TabPopFragment")
                        .commit();
            } else {
                onBackPressed();
            }
        });

        mPagerAdapter = new ResourcePagerAdapter(this);
        for (String string : mTitlesO) {
            mTriples.add(Triple.create(string, null, 0));
        }
        mPagerAdapter.setData(mTriples);
        vp_resource.setOffscreenPageLimit(3);
        vp_resource.setAdapter(mPagerAdapter);
        mTabLayoutMediator = new TabLayoutMediator(tab_resource, vp_resource, (tab, position) -> {
            Triple<String, String, Integer> triple = mTriples.get(position);
            tab.setText(triple.first + "[" + triple.third + "]");
        });
        mTabLayoutMediator.attach();

        mDisposable = RxBus.getInstance().asFlowable()
                .filter(o -> o instanceof TitleChangeEvent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    TitleChangeEvent e = (TitleChangeEvent) o;
                    for (int i = 0; i < mTitlesO.size(); i++) {
                        if (TextUtils.equals(mTitlesO.get(i), e.title)) {
                            mTriples.set(i, Triple.create(e.title, null, e.count));
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

    private static class ResourcePagerAdapter extends FragmentStateAdapter {

        private List<Triple<String, String, Integer>> mData;

        public ResourcePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return ResourcesFragment.newInstance(mData.get(position).first, mData.get(position).second);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public long getItemId(int position) {
            String name = mData.get(position).first;
            assert name != null;
            return name.hashCode();
        }

        @Override
        public boolean containsItem(long itemId) {
            for (Triple<String, String, Integer> p : mData) {
                String name = p.first;
                assert name != null;
                if (name.hashCode() == itemId) {
                    return true;
                }
            }
            return false;
        }

        public void setData(List<Triple<String, String, Integer>> data) {
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

    public static class ResourcesFragment extends RecyclerFragment<ResourceEntity> {

        private View mEmptyView;
        private SlimAdapter mAdapter;

        private String mTabTitle;
        private String mDir;

        private Disposable mDisposableR;
        private boolean mNeedRefresh;

        private static ResourcesFragment newInstance(String tabTitle, String dir) {
            ResourcesFragment fragment = new ResourcesFragment();
            fragment.mTabTitle = tabTitle;
            fragment.mDir = dir;
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mDisposableR = RxBus.getInstance()
                    .asFlowable()
                    .filter(o -> o instanceof RefreshResourcesEvent)
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
        public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> onCreateAdapter(List<ResourceEntity> data) {
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
                                    .putExtra("resource_id", viewHolder.itemData.id)));
                        }

                        @Override
                        public void onBind(ResourceEntity data, SlimAdapter.SlimViewHolder<ResourceEntity> viewHolder) {
                            String applyStr;
                            int color;
                            boolean visible;
                            if (TextUtils.equals("1", data.applyStatus)) {
                                applyStr = "待审核";
                                color = 0xffdf8b07;
                                visible = true;
                            } else if (TextUtils.equals("2", data.applyStatus)) {
                                applyStr = "审核通过";
                                color = 0xff1eb01b;
                                visible = true;
                            } else if (TextUtils.equals("3", data.applyStatus)) {
                                applyStr = "审核未通过!";
                                color = 0xffd23319;
                                visible = true;
                            } else {
                                applyStr = "暂无使用权限";
                                color = 0xffd23319;
                                visible = false;
                            }
                            Spans spans = new Spans().append(data.tableComment);
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
                                    .text(R.id.tv_share_method, "共享类型：" + data.permission)
                                    .text(R.id.tv_supplier, "管理单位：" + (TextUtils.isEmpty(data.unitName) ? "暂无" : data.unitName))
                                    .text(R.id.tv_publish_time, "发布时间：" + data.createDate)
                                    .text(R.id.tv_count_1, new Spans().append(String.valueOf(data.browses)));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<ResourceEntity>> onCreatePageObservable(int currentPage) {
            return Single.just(Triple.create(mTabTitle, currentPage, mDir))
                    .flatMap(pair -> {
                        String s = pair.first;
                        Integer p = pair.second;
                        String dir = pair.third;
                        if (TextUtils.equals("已申请", s)) {
                            return RestAPI.getInstance().apiServiceSB()
                                    .getResourcesA(SharedPreferencesDataSource.getSignInResponse().user.id, p, 20);
                        } else if (TextUtils.equals("已收藏", s)) {
                            return RestAPI.getInstance().apiServiceSB()
                                    .getResourcesF(SharedPreferencesDataSource.getSignInResponse().user.id, p, 20);
                        } else {
                            return RestAPI.getInstance().apiServiceSB()
                                    .getResources(dir, SharedPreferencesDataSource.getSignInResponse().user.id, null, null, p, 20);
                        }
                    })
                    .map(responseData -> {
                        PageResult<ResourceEntity> pageResult = new PageResult<>();
                        if (responseData.data.result == null) {
                            responseData.data.result = new ArrayList<>();
                        }
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
        public void notifyDataChanged(List<ResourceEntity> data) {
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

    static class RefreshResourcesEvent {
    }
}

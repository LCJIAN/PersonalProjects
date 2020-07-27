package com.org.firefighting.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lcjian.lib.content.SimpleFragmentPagerAdapter;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.lib.util.common.DimenUtils;
import com.org.firefighting.BuildConfig;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.News;
import com.org.firefighting.data.network.entity.Task;
import com.org.firefighting.data.network.entity.Weather;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.common.FCaptureActivity;
import com.org.firefighting.ui.common.NewsActivity;
import com.org.firefighting.ui.common.SearchActivity;
import com.org.firefighting.ui.common.StaticsActivity;
import com.org.firefighting.ui.common.WebViewActivity;
import com.org.firefighting.ui.resource.ResourcesActivity;
import com.org.firefighting.ui.service.ServiceListActivity;
import com.org.firefighting.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import q.rorbin.badgeview.Badge;
import q.rorbin.badgeview.QBadgeView;
import timber.log.Timber;

public class HomeFragment extends BaseFragment {

    @BindView(R.id.tv_go_to_search)
    TextView tv_go_to_search;
    @BindView(R.id.btn_go_to_scan)
    ImageButton btn_go_to_scan;
    @BindView(R.id.srl_home)
    SwipeRefreshLayout srl_home;
    @BindView(R.id.fl_task)
    FrameLayout fl_task;
    @BindView(R.id.tv_task)
    TextView tv_task;
    @BindView(R.id.tv_organization)
    TextView tv_organization;
    @BindView(R.id.tv_announcement)
    TextView tv_announcement;
    @BindView(R.id.tv_helping)
    TextView tv_helping;

    @BindView(R.id.cl_weather)
    ConstraintLayout cl_weather;
    @BindView(R.id.iv_weather)
    ImageView iv_weather;
    @BindView(R.id.tv_weather)
    TextView tv_weather;
    @BindView(R.id.tv_now)
    TextView tv_now;

    @BindView(R.id.tv_duty_statics)
    TextView tv_duty_statics;
    @BindView(R.id.tv_real_time_statics)
    TextView tv_real_time_statics;
    @BindView(R.id.tv_police_statics)
    TextView tv_police_statics;
    @BindView(R.id.vp_statics)
    ViewPager vp_statics;

    @BindView(R.id.tv_task_input)
    TextView tv_task_input;
    @BindView(R.id.tv_task_check)
    TextView tv_task_check;
    @BindView(R.id.tv_task_data)
    TextView tv_task_data;
    @BindView(R.id.rv_summary)
    RecyclerView rv_summary;

    @BindView(R.id.tv_view_more_news)
    TextView tv_view_more_news;
    @BindView(R.id.rv_news)
    RecyclerView rv_news;

    private Unbinder mUnBinder;

    private Disposable mDisposable;
    private Disposable mDisposableW;
    private Disposable mDisposableS;
    private Disposable mDisposableC;
    private Disposable mDisposableN;

    private List<SummaryItem> mSummaryItems1;
    private List<SummaryItem> mSummaryItems2;
    private List<SummaryItem> mSummaryItems3;
    private SlimAdapter mSummaryAdapter;

    private SlimAdapter mNewsAdapter;

    private Badge mBadge;

    private int mStaticsCheckId;
    private int mSummaryCheckId;

    private ViewPager.SimpleOnPageChangeListener mStaticsPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            Context context = vp_statics.getContext();
            ViewGroup.LayoutParams layoutParams = vp_statics.getLayoutParams();
            if (position == 0) {
                layoutParams.height = (int) DimenUtils.dipToPixels(400, context);
                setupStaticsTab(tv_duty_statics.getId());
            } else if (position == 1) {
                layoutParams.height = (int) DimenUtils.dipToPixels(400, context);
                setupStaticsTab(tv_real_time_statics.getId());
            } else {
                layoutParams.height = (int) DimenUtils.dipToPixels(200, context);
                setupStaticsTab(tv_police_statics.getId());
            }
            vp_statics.setLayoutParams(layoutParams);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mBadge = new QBadgeView(tv_task.getContext()).bindTarget(tv_task);

        tv_go_to_search.setOnClickListener(v -> startActivity(new Intent(v.getContext(), SearchActivity.class)));
        btn_go_to_scan.setOnClickListener(v -> IntentIntegrator.forSupportFragment(this).setCaptureActivity(FCaptureActivity.class).initiateScan());

        fl_task.setOnClickListener(v -> ((MainActivity) getActivity()).checkTask());
        tv_organization.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ResourcesActivity.class)));
        tv_announcement.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ServiceListActivity.class)));
        tv_helping.setOnClickListener(v -> startActivity(new Intent(v.getContext(), StaticsActivity.class)));

        cl_weather.setOnClickListener(v -> startActivity(new Intent(v.getContext(), WebViewActivity.class)
                .putExtra("url", "https://weather.seniverse.com/?token=a50504dd-e2a1-45b6-8de5-4007e9ab3137")));

        tv_duty_statics.setOnClickListener(v -> setupStaticsTab(v.getId()));
        tv_real_time_statics.setOnClickListener(v -> setupStaticsTab(v.getId()));
        tv_police_statics.setOnClickListener(v -> setupStaticsTab(v.getId()));
        tv_view_more_news.setOnClickListener(v -> startActivity(new Intent(v.getContext(), NewsActivity.class)));

        tv_task_input.setOnClickListener(v -> setupSummaryTab(v.getId()));
        tv_task_check.setOnClickListener(v -> setupSummaryTab(v.getId()));
        tv_task_data.setOnClickListener(v -> setupSummaryTab(v.getId()));

        srl_home.setColorSchemeResources(R.color.colorPrimary);
        srl_home.setOnRefreshListener(this::getData);

        mSummaryAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<SummaryItem>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.task_summary_item;
            }

            @Override
            public void onBind(SummaryItem data, SlimAdapter.SlimViewHolder<SummaryItem> viewHolder) {
                viewHolder.text(R.id.tv_name, data.name)
                        .text(R.id.tv_count, String.valueOf(data.count));
            }
        });
        rv_summary.setHasFixedSize(false);
        rv_summary.setLayoutManager(new GridLayoutManager(view.getContext(), 3));
        rv_summary.setAdapter(mSummaryAdapter);

        mNewsAdapter = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<News>() {
                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.home_news_item;
                    }

                    @Override
                    public void onInit(SlimAdapter.SlimViewHolder<News> viewHolder) {
                        viewHolder.clicked(v -> startActivity(new Intent(v.getContext(), WebViewActivity.class)
                                .putExtra("url", BuildConfig.API_URL_SB + "crawler" + viewHolder.itemData.filePath + "/" + viewHolder.itemData.fileName)));
                    }

                    @Override
                    public void onBind(News data, SlimAdapter.SlimViewHolder<News> viewHolder) {
                        viewHolder.background(R.id.ll_news, new ColorDrawable(viewHolder.getBindingAdapterPosition() % 2 == 0 ? 0xffeeeef5 : 0xffffffff))
                                .text(R.id.tv_title, data.title)
                                .text(R.id.tv_time, data.publishTime);
                    }
                });
        rv_news.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv_news.setAdapter(mNewsAdapter);


        vp_statics.addOnPageChangeListener(mStaticsPageChangeListener);
        getData();
    }

    @Override
    public void onResume() {
        super.onResume();
        getNow();
    }

    @Override
    public void onPause() {
        mDisposableC.dispose();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        vp_statics.removeOnPageChangeListener(mStaticsPageChangeListener);
        mDisposable.dispose();
        if (mDisposableW != null) {
            mDisposableW.dispose();
        }
        if (mDisposableS != null) {
            mDisposableS.dispose();
        }
        mUnBinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String content = result.getContents();
            if (!TextUtils.isEmpty(content)) {
                Uri uri = Uri.parse(content);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getWeather() {
        mDisposableW = Single.just("http://wthrcdn.etouch.cn/weather_mini?city=重庆")
                .map(aString -> new Gson().fromJson(Utils.get(aString, null, null), Weather.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(weather -> {
                            Weather.Forecast forecast = weather.data.forecast.get(0);

                            if (!TextUtils.isEmpty(forecast.type)) {
                                if (forecast.type.contains("雪")) {
                                    iv_weather.setImageResource(R.drawable.dr1);
                                } else if (forecast.type.contains("雷")) {
                                    iv_weather.setImageResource(R.drawable.dr8);
                                } else if (forecast.type.contains("雨")) {
                                    iv_weather.setImageResource(R.drawable.dr5);
                                } else if (forecast.type.contains("云")) {
                                    iv_weather.setImageResource(R.drawable.dr13);
                                } else if (forecast.type.contains("阴")) {
                                    iv_weather.setImageResource(R.drawable.dr16);
                                } else {
                                    iv_weather.setImageResource(R.drawable.dr11);
                                }
                            }
                            tv_weather.setText(Html.fromHtml(forecast.type + " " +
                                    forecast.low.replace("低温", "") + "~" + forecast.high.replace("高温", "") + " " +
                                    forecast.fengxiang + " " + forecast.fengli));
                        },
                        Timber::e);
    }

    private void getData() {
        setRefreshing(true);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = RestAPI.getInstance().apiService().getMyTasks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pageResponse -> {
                    setRefreshing(false);
                    int i = 0;
                    if (pageResponse != null && pageResponse.result != null) {
                        for (Task task : pageResponse.result) {
                            if (task.endDate == null) {
                                continue;
                            }
                            if (TextUtils.equals(task.myWorkStatus.workStatus, "待查看")) {
                                i++;
                            }
                        }
                    }
                    mBadge.setBadgeNumber(i);
                    getSummary();
                    getWeather();
                    getNews();
                    vp_statics.setOffscreenPageLimit(3);
                    vp_statics.setAdapter(new SimpleFragmentPagerAdapter(getChildFragmentManager())
                            .addFragment(new DutyStaticsFragment(), "值班动态")
                            .addFragment(new RealTimeStaticsFragment(), "实时警情")
                            .addFragment(new PoliceStaticsFragment(), "警情数据"));

                    int staticsCheckId;
                    if (mStaticsCheckId == 0) {
                        staticsCheckId = tv_duty_statics.getId();
                    } else {
                        staticsCheckId = mStaticsCheckId;
                        mStaticsCheckId = 0;
                    }
                    setupStaticsTab(staticsCheckId);
                }, throwable -> {
                    setRefreshing(false);
                    ThrowableConsumerAdapter.accept(throwable);
                });
    }

    private void getSummary() {
        if (mDisposableS != null) {
            mDisposableS.dispose();
        }
        mDisposableS = RestAPI.getInstance().apiService()
                .getTaskSummary1(SharedPreferencesDataSource.getSignInResponse().user.id,
                        SharedPreferencesDataSource.getSignInResponse().token)
                .map(mapResponseData -> {
                    List<SummaryItem> list = new ArrayList<>();
                    for (Map.Entry<String, Integer> entry : mapResponseData.data.entrySet()) {
                        list.add(new SummaryItem(entry.getKey(), entry.getValue()));
                    }
                    return list;
                })
                .zipWith(RestAPI.getInstance().apiService().getTaskSummary2()
                        .map(taskSummary2ResponseData -> {
                            List<SummaryItem> list1 = new ArrayList<>();
                            List<SummaryItem> list2 = new ArrayList<>();

                            list1.add(new SummaryItem("外部单位认证申请", taskSummary2ResponseData.org_apply_num));
                            list1.add(new SummaryItem("实时服务申请", taskSummary2ResponseData.service_apply_num));
                            list1.add(new SummaryItem("资源目录申请", taskSummary2ResponseData.resource_apply_num));
                            list1.add(new SummaryItem("数据源注册申请", Integer.parseInt(taskSummary2ResponseData.ds_yest_total)));

                            list2.add(new SummaryItem("待整改", Integer.parseInt(taskSummary2ResponseData.not_valid_yest_total)));
                            list2.add(new SummaryItem("已整改", Integer.parseInt(taskSummary2ResponseData.have_valid_yest_total)));
                            return Pair.create(list1, list2);
                        }), Pair::create)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                            assert pair.first != null;
                            assert pair.second != null;
                            mSummaryItems1 = pair.first;
                            mSummaryItems2 = pair.second.first;
                            mSummaryItems3 = pair.second.second;

                            setupSummaryTab(tv_task_input.getId());
                        },
                        ThrowableConsumerAdapter::accept);
    }

    private void getNow() {
        mDisposableC = Flowable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> tv_now.setText(DateUtils.convertDateToStr(DateUtils.now(), "yyyy-MM-dd EEE a HH:mm:ss")));
    }

    private void getNews() {
        mDisposableN = RestAPI.getInstance().apiServiceSB()
                .getNews(null, 1, 2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newsPageResponse -> mNewsAdapter.updateData(newsPageResponse.result),
                        ThrowableConsumerAdapter::accept);
    }

    private void setupStaticsTab(int staticsCheckId) {
        if (mStaticsCheckId == staticsCheckId) {
            return;
        }
        mStaticsCheckId = staticsCheckId;
        if (tv_duty_statics.getId() == staticsCheckId) {
            setupCheckedTab(tv_duty_statics, R.drawable.summary_tab_bg1);
            setupUnCheckedTab(tv_real_time_statics);
            setupUnCheckedTab(tv_police_statics);
            vp_statics.setCurrentItem(0);
        }
        if (tv_real_time_statics.getId() == staticsCheckId) {
            setupCheckedTab(tv_real_time_statics, R.drawable.summary_tab_bg2);
            setupUnCheckedTab(tv_duty_statics);
            setupUnCheckedTab(tv_police_statics);
            vp_statics.setCurrentItem(1);
        }
        if (tv_police_statics.getId() == staticsCheckId) {
            setupCheckedTab(tv_police_statics, R.drawable.summary_tab_bg3);
            setupUnCheckedTab(tv_real_time_statics);
            setupUnCheckedTab(tv_duty_statics);
            vp_statics.setCurrentItem(2);
        }
    }

    private void setupSummaryTab(int summaryCheckId) {
        if (mSummaryCheckId == summaryCheckId) {
            return;
        }
        mSummaryCheckId = summaryCheckId;
        if (tv_task_input.getId() == summaryCheckId) {
            setupCheckedTab(tv_task_input, R.drawable.summary_tab_bg1);
            setupUnCheckedTab(tv_task_check);
            setupUnCheckedTab(tv_task_data);

            mSummaryAdapter.updateData(mSummaryItems1);
        }
        if (tv_task_check.getId() == summaryCheckId) {
            setupCheckedTab(tv_task_check, R.drawable.summary_tab_bg2);
            setupUnCheckedTab(tv_task_input);
            setupUnCheckedTab(tv_task_data);

            mSummaryAdapter.updateData(mSummaryItems2);
        }
        if (tv_task_data.getId() == summaryCheckId) {
            setupCheckedTab(tv_task_data, R.drawable.summary_tab_bg3);
            setupUnCheckedTab(tv_task_input);
            setupUnCheckedTab(tv_task_check);

            mSummaryAdapter.updateData(mSummaryItems3);
        }
    }

    private void setupCheckedTab(TextView tab, int res) {
        tab.setTextColor(0xff1f7fe4);
        tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tab.setBackgroundResource(res);
    }

    private void setupUnCheckedTab(TextView tab) {
        tab.setTextColor(0xff333333);
        tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tab.setBackgroundColor(ContextCompat.getColor(tab.getContext(), android.R.color.transparent));
    }

    private void setRefreshing(final boolean refreshing) {
        if (srl_home.isEnabled()) {
            srl_home.post(() -> {
                if (srl_home != null) {
                    srl_home.setRefreshing(refreshing);
                }
            });
        }
    }

    private static class SummaryItem {
        private String name;
        private Integer count;

        private SummaryItem(String name, Integer count) {
            this.name = name;
            this.count = count;
        }
    }
}

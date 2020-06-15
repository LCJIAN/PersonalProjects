package com.org.firefighting.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.Task;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.common.FCaptureActivity;
import com.org.firefighting.ui.common.NewsActivity;
import com.org.firefighting.ui.common.SearchActivity;
import com.org.firefighting.ui.resource.ResourcesActivity;
import com.org.firefighting.ui.service.ServiceListActivity;
import com.org.firefighting.ui.task.TaskDetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import q.rorbin.badgeview.QBadgeView;

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

    @BindView(R.id.tv_task_input)
    TextView tv_task_input;
    @BindView(R.id.tv_task_check)
    TextView tv_task_check;
    @BindView(R.id.tv_task_data)
    TextView tv_task_data;
    @BindView(R.id.rv_summary)
    RecyclerView rv_summary;

    private Unbinder mUnBinder;

    private Disposable mDisposable;
    private Disposable mDisposableS;

    private List<SummaryItem> mSummaryItems1;
    private List<SummaryItem> mSummaryItems2;
    private List<SummaryItem> mSummaryItems3;
    private SlimAdapter mSummaryAdapter;

    private int mSummaryCheckId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_go_to_search.setOnClickListener(v -> startActivity(new Intent(v.getContext(), SearchActivity.class)));
        btn_go_to_scan.setOnClickListener(v -> IntentIntegrator.forSupportFragment(this).setCaptureActivity(FCaptureActivity.class).initiateScan());
        fl_task.setOnClickListener(v -> ((MainActivity) getActivity()).checkTask());
        tv_organization.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ResourcesActivity.class)));
        tv_announcement.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ServiceListActivity.class)));
        tv_helping.setOnClickListener(v -> startActivity(new Intent(v.getContext(), NewsActivity.class)));
        tv_task_input.setOnClickListener(v -> setupSummaryTab(v.getId()));
        tv_task_check.setOnClickListener(v -> setupSummaryTab(v.getId()));
        tv_task_data.setOnClickListener(v -> setupSummaryTab(v.getId()));

        srl_home.setColorSchemeResources(R.color.colorPrimary);
        srl_home.setOnRefreshListener(this::setupContent);

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

        setupContent();
    }

    @Override
    public void onDestroyView() {
        mDisposable.dispose();
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
                String scheme = uri.getScheme();
                if (TextUtils.equals("com.org.firefighting", scheme)) {
                    startActivity(new Intent(getContext(), TaskDetailActivity.class)
                            .putExtra("task_id", uri.getQueryParameter("taskid")));
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupContent() {
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
                    for (Task task : pageResponse.result) {
                        if (task.endDate == null) {
                            continue;
                        }
                        if (TextUtils.equals(task.myWorkStatus.workStatus, "待查看")) {
                            i++;
                        }
                    }
                    new QBadgeView(tv_task.getContext()).bindTarget(tv_task).setBadgeNumber(i);
                    setupSummary();
                }, throwable -> {
                    setRefreshing(false);
                    ThrowableConsumerAdapter.accept(throwable);
                });
    }

    private void setupSummary() {
        if (mDisposableS != null) {
            mDisposableS.dispose();
        }
        mDisposableS = RestAPI.getInstance().apiService()
                .getTaskSummary1(SharedPreferencesDataSource.getSignInResponse().token)
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
                            list1.add(new SummaryItem("服务目录申请", taskSummary2ResponseData.service_apply_num));
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

    private void setupSummaryTab(int summaryCheckId) {
        if (mSummaryCheckId == summaryCheckId) {
            return;
        }
        mSummaryCheckId = summaryCheckId;
        if (tv_task_input.getId() == summaryCheckId) {
            setupCheckedSummaryTab(tv_task_input, R.drawable.summary_tab_bg1);
            setupUnCheckedSummaryTab(tv_task_check);
            setupUnCheckedSummaryTab(tv_task_data);

            mSummaryAdapter.updateData(mSummaryItems1);
        }
        if (tv_task_check.getId() == summaryCheckId) {
            setupCheckedSummaryTab(tv_task_check, R.drawable.summary_tab_bg2);
            setupUnCheckedSummaryTab(tv_task_input);
            setupUnCheckedSummaryTab(tv_task_data);

            mSummaryAdapter.updateData(mSummaryItems2);
        }
        if (tv_task_data.getId() == summaryCheckId) {
            setupCheckedSummaryTab(tv_task_data, R.drawable.summary_tab_bg3);
            setupUnCheckedSummaryTab(tv_task_input);
            setupUnCheckedSummaryTab(tv_task_check);

            mSummaryAdapter.updateData(mSummaryItems3);
        }
    }

    private void setupCheckedSummaryTab(TextView tab, int res) {
        tab.setTextColor(0xff1f7fe4);
        tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tab.setBackgroundResource(res);
    }

    private void setupUnCheckedSummaryTab(TextView tab) {
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

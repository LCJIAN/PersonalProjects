package com.org.firefighting.ui.task;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.widget.NestedScrollWebView;
import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.Task;
import com.org.firefighting.data.network.entity.TaskTable;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.base.ConfirmFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ExcelFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.rv_table_name)
    RecyclerView rv_table_name;

    @BindView(R.id.web_view)
    NestedScrollWebView webView;
    @BindView(R.id.fl_add)
    FrameLayout fl_add;
    @BindView(R.id.fl_clear)
    FrameLayout fl_clear;

    private Unbinder mUnBinder;

    private SlimAdapter mAdapter;

    private String mTaskId;

    private Disposable mDisposable;
    private Disposable mDisposableR;
    private Disposable mDisposableC;
    private Disposable mDisposableD;

    private TaskTable mChecked;

    private Task mTask;

    public static ExcelFragment newInstance(String taskId) {
        ExcelFragment fragment = new ExcelFragment();
        Bundle args = new Bundle();
        args.putString("task_id", taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTaskId = getArguments().getString("task_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_excel, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WebSettings webSettings = webView.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JsBridge(), "jsBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                showProgress();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                hideProgress();
            }
        });

        fl_add.setOnClickListener(this);
        fl_clear.setOnClickListener(this);

        rv_table_name.setHasFixedSize(true);
        rv_table_name.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));

        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<TaskTable>() {

            @Override
            public int onGetLayoutResource() {
                return R.layout.table_name_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<TaskTable> viewHolder) {
                viewHolder.clicked(v -> {
                    mChecked = viewHolder.itemData;
                    setupWeb();
                    mAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onBind(TaskTable data, SlimAdapter.SlimViewHolder<TaskTable> viewHolder) {
                viewHolder.text(R.id.tv_table_name, data.name)
                        .background(R.id.tv_table_name, mChecked == data ? R.drawable.shape_tab_checked : R.drawable.shape_tab_normal)
                        .textColor(R.id.tv_table_name, mChecked == data ? 0xffeeeef5 : 0xffbcbcbc);
            }
        });
        rv_table_name.setAdapter(mAdapter);

        mDisposableR = RxBus.getInstance().asFlowable()
                .filter(o -> o instanceof Task)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(q -> mTask = (Task) q);

        setupContent();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mChecked != null) {
            setupWeb();
        }
    }

    @Override
    public void onDestroyView() {
        mDisposable.dispose();
        mDisposableR.dispose();
        if (mDisposableC != null) {
            mDisposableC.dispose();
        }
        if (mDisposableD != null) {
            mDisposableD.dispose();
        }
        mUnBinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_add:
                if (mTask != null
                        && ((TextUtils.equals(mTask.myWorkStatus.workStatus, "待查看")
                        || TextUtils.equals(mTask.myWorkStatus.workStatus, "填报中"))
                        && !TextUtils.equals(mTask.status, "已完结"))) {
                    if (mChecked != null) {
                        startActivity(new Intent(v.getContext(), AddRecordActivity.class)
                                .putExtra("task_id", mTaskId)
                                .putExtra("task_table", mChecked));
                    }
                } else {
                    Toast.makeText(App.getInstance(), "当前任务已填报/已完结", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.fl_clear:
                if (mTask != null
                        && ((TextUtils.equals(mTask.myWorkStatus.workStatus, "待查看")
                        || TextUtils.equals(mTask.myWorkStatus.workStatus, "填报中"))
                        && !TextUtils.equals(mTask.status, "已完结"))) {
                    showClearConfirm();
                } else {
                    Toast.makeText(App.getInstance(), "当前任务已填报/已完结", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void setupContent() {
        showProgress();
        mDisposable = RestAPI.getInstance().apiService().getTaskTables(mTaskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResponseData -> {
                            hideProgress();

                            mChecked = listResponseData.data.get(0);
                            setupWeb();
                            mAdapter.updateData(listResponseData.data);
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void setupWeb() {
        webView.loadUrl("http://utils.chimukeji.com/fire_brigade/index.html?" +
                "taskid=" + mTaskId +
                "&tableid=" + mChecked.id +
                "&token=" + SharedPreferencesDataSource.getSignInResponse().token);
    }

    private void showClearConfirm() {
        new ConfirmFragment()
                .setTitleProvider(() -> "是否清空当前录入数据？")
                .setListener(new ConfirmFragment.SimpleListener() {
                    @Override
                    public void onConfirm() {
                        clearRecords();
                    }
                }).show(getChildFragmentManager(), "ConfirmFragment");
    }

    private void clearRecords() {
        showProgress();
        if (mDisposableD != null) {
            mDisposableD.dispose();
        }
        mDisposableD = RestAPI.getInstance().apiService().clearTaskTableRecords(mTaskId, mChecked.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), objectResponseData.message, Toast.LENGTH_SHORT).show();
                            if (objectResponseData.code == 0) {
                                setupWeb();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void deleteRecord(int position) {
        showProgress();
        if (mDisposableC != null) {
            mDisposableC.dispose();
        }
        mDisposableC = RestAPI.getInstance().apiService().deleteTaskTableRecord(mTaskId, mChecked.id, position)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objectResponseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), objectResponseData.message, Toast.LENGTH_SHORT).show();
                            if (objectResponseData.code == 0) {
                                setupWeb();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }


    private class JsBridge {

        @JavascriptInterface
        public void confirmToDeleteRecord(int position) {
            new ConfirmFragment()
                    .setTitleProvider(() -> "确认要删除本条记录？")
                    .setListener(new ConfirmFragment.SimpleListener() {
                        @Override
                        public void onConfirm() {
                            deleteRecord(position);
                        }
                    }).show(getChildFragmentManager(), "ConfirmFragment");
        }

        @JavascriptInterface
        public void navigateToModifyRecord(int position) {
            startActivity(new Intent(getContext(), AddRecordActivity.class)
                    .putExtra("task_id", mTaskId)
                    .putExtra("task_table", mChecked)
                    .putExtra("position", position));
        }
    }
}

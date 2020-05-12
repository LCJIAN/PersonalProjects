package com.org.firefighting.ui.task;

import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.FragmentSwitchHelper;
import com.lcjian.lib.util.common.DateUtils;
import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.AskRequest;
import com.org.firefighting.data.network.entity.ResponseData;
import com.org.firefighting.data.network.entity.Task;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.base.ConfirmFragment;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TaskDetailActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.btn_back)
    ImageButton btn_back;

    @BindView(R.id.tv_task_end_date)
    TextView tv_task_end_date;
    @BindView(R.id.tv_task_remaining)
    TextView tv_task_remaining;
    @BindView(R.id.tv_task_status)
    TextView tv_task_status;

    @BindView(R.id.tv_start_date)
    TextView tv_start_date;
    @BindView(R.id.tv_task_title)
    TextView tv_task_title;
    @BindView(R.id.tv_task_from)
    TextView tv_task_from;
    @BindView(R.id.tv_task_detail)
    TextView tv_task_detail;

    @BindView(R.id.tab_task_detail)
    TabLayout tab_task_detail;

    @BindView(R.id.rl_submit)
    RelativeLayout rl_submit;
    @BindView(R.id.btn_submit)
    Button btn_submit;

    @BindView(R.id.rl_ask)
    LinearLayout rl_ask;
    @BindView(R.id.et_ask)
    EditText et_ask;
    @BindView(R.id.btn_ask)
    Button btn_ask;

    @BindView(R.id.app_bar)
    AppBarLayout app_bar;

    private String mTaskId;

    private Disposable mDisposable;
    private Disposable mDisposableAsk;
    private Disposable mDisposableUpload;

    private FragmentSwitchHelper mFragmentSwitchHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        ButterKnife.bind(this);

        Uri uri = getIntent().getData();
        if (uri != null) {
            mTaskId = uri.getQueryParameter("taskid");
        } else {
            mTaskId = getIntent().getStringExtra("task_id");
        }

        mFragmentSwitchHelper = FragmentSwitchHelper.create(R.id.fl_fragment_container,
                getSupportFragmentManager(), true,
                ExcelFragment.newInstance(mTaskId), QAFragment.newInstance(mTaskId), LogFragment.newInstance(mTaskId));
        tab_task_detail.addTab(tab_task_detail.newTab().setText("任务表单"));
        tab_task_detail.addTab(tab_task_detail.newTab().setText("任务答疑"));
        tab_task_detail.addTab(tab_task_detail.newTab().setText("任务日志"));
        tab_task_detail.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: {
                        mFragmentSwitchHelper.changeFragment(ExcelFragment.class);
                        rl_submit.setVisibility(View.VISIBLE);
                        rl_ask.setVisibility(View.GONE);
                    }
                    break;
                    case 1: {
                        mFragmentSwitchHelper.changeFragment(QAFragment.class);
                        rl_submit.setVisibility(View.GONE);
                        rl_ask.setVisibility(View.VISIBLE);
                    }
                    break;
                    case 2: {
                        mFragmentSwitchHelper.changeFragment(LogFragment.class);
                        rl_submit.setVisibility(View.GONE);
                        rl_ask.setVisibility(View.GONE);
                    }
                    break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        btn_back.setOnClickListener(this);
        btn_ask.setOnClickListener(this);
        btn_submit.setOnClickListener(this);

        app_bar.post(() -> ((AppBarLayout.Behavior)
                Objects.requireNonNull(((CoordinatorLayout.LayoutParams) app_bar.getLayoutParams())
                        .getBehavior()))
                .setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                    @Override
                    public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                        return true;
                    }
                }));

        mFragmentSwitchHelper.changeFragment(ExcelFragment.class);
        setupDetail();
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableAsk != null) {
            mDisposableAsk.dispose();
        }
        if (mDisposableUpload != null) {
            mDisposableUpload.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                showDataUploadConfirm();
                break;
            case R.id.btn_ask:
                if (!TextUtils.isEmpty(et_ask.getEditableText())) {
                    askQuestion();
                } else {
                    Toast.makeText(App.getInstance(), "请输入问题内容", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private void setupDetail() {
        showProgress();
        mDisposable = RestAPI.getInstance().apiService().checkTaskDetail(mTaskId)
                .flatMap(accessResponseData -> {
                    if (accessResponseData.code == -1) {
                        return Single.just(new ResponseData<Task>());
                    } else {
                        return RestAPI.getInstance().apiService().getTaskDetail(mTaskId);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskResponseData -> {
                            hideProgress();
                            if (taskResponseData.data == null) {
                                Toast.makeText(App.getInstance(), "你不在任务填报范围内", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                            Task task = taskResponseData.data;

                            tv_task_end_date.setText(new Spans("任务截至：").append(task.endDate));
                            tv_task_status.setText(new Spans("状态：").append(task.status));
                            tv_task_remaining.setText(new Spans("状态：").append(task.status));

                            String f = "yyyy-MM-dd";
                            int days = (int)
                                    ((DateUtils.convertStrToDate(task.endDate.split(" ")[0], f).getTime()
                                            - DateUtils.convertStrToDate(DateUtils.convertDateToStr(DateUtils.now(), f), f).getTime()) / (1000 * 3600 * 24));
                            if (days <= 0) {
                                tv_task_remaining.setText(new Spans("超").append(String.valueOf(Math.abs(days))).append("天"));
                                tv_task_remaining.setTextColor(0xffff0000);
                            } else {
                                tv_task_remaining.setText(new Spans("剩").append(String.valueOf(days)).append("天"));
                                tv_task_remaining.setTextColor(0xff19d100);
                            }

                            tv_start_date.setText(task.startDate);
                            tv_task_title.setText(task.name);
                            tv_task_from.setText(new Spans(task.departmentName).append("【").append(task.code).append("】 号 "));
                            tv_task_detail.setText(Html.fromHtml(task.description));

                            if ((TextUtils.equals(task.myWorkStatus.workStatus, "待查看")
                                    || TextUtils.equals(task.myWorkStatus.workStatus, "填报中"))
                                    && !TextUtils.equals(task.status, "已完结")) {
                                btn_submit.setEnabled(true);
                                et_ask.setEnabled(true);
                                btn_ask.setEnabled(true);
                            } else {
                                btn_submit.setEnabled(false);
                                et_ask.setEnabled(false);
                                btn_ask.setEnabled(false);
                            }

                            RxBus.getInstance().send(task);
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void showDataUploadConfirm() {
        new ConfirmFragment()
                .setTitleProvider(() -> "是否上报？")
                .setListener(new ConfirmFragment.SimpleListener() {
                    @Override
                    public void onConfirm() {
                        dataUpload();
                    }
                }).show(getSupportFragmentManager(), "ConfirmFragment");
    }

    private void askQuestion() {
        AskRequest askRequest = new AskRequest();
        askRequest.question = et_ask.getEditableText().toString();
        showProgress();
        if (mDisposableAsk != null) {
            mDisposableAsk.dispose();
        }
        mDisposableAsk = RestAPI.getInstance().apiService().putTaskQuestion(mTaskId, askRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskQuestionResponseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), taskQuestionResponseData.message, Toast.LENGTH_SHORT).show();
                            if (taskQuestionResponseData.code == 0) {
                                et_ask.setText("");
                                RxBus.getInstance().send(taskQuestionResponseData.data);
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void dataUpload() {
        if (mDisposableUpload != null) {
            mDisposableUpload.dispose();
        }
        mDisposableUpload = RestAPI.getInstance().apiService().uploadTaskData(mTaskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskQuestionResponseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), taskQuestionResponseData.message, Toast.LENGTH_SHORT).show();
                            if (taskQuestionResponseData.code == 0) {
                                finish();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }
}

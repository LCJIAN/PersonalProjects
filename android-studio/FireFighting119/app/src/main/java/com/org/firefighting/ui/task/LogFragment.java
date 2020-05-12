package com.org.firefighting.ui.task;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.util.common.DateUtils;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.TaskLog;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.Collections;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LogFragment extends BaseFragment {

    private RecyclerView rv_task_log;

    private SlimAdapter mAdapter;

    private String mTaskId;

    private Disposable mDisposable;

    public static LogFragment newInstance(String taskId) {
        LogFragment fragment = new LogFragment();
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
        View view = inflater.inflate(R.layout.fragment_task_log, container, false);
        rv_task_log = view.findViewById(R.id.rv_task_log);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv_task_log.setHasFixedSize(false);
        rv_task_log.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<TaskLog>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.task_log_item;
            }

            @Override
            public void onBind(TaskLog data, SlimAdapter.SlimViewHolder<TaskLog> viewHolder) {
                viewHolder.text(R.id.tv_log_date, DateUtils.convertDateToStr(new Date(data.infoTime), DateUtils.YYYY_MM_DD))
                        .text(R.id.tv_log_time, DateUtils.convertDateToStr(new Date(data.infoTime), DateUtils.HH_MM_SS))
                        .text(R.id.tv_log_detail, data.departmentName + " " + Html.fromHtml(data.message))
                        .visibility(R.id.divider4, viewHolder.getBindingAdapterPosition() == mAdapter.getItemCount() - 1 ? View.INVISIBLE : View.VISIBLE)
                        .visibility(R.id.divider5, viewHolder.getBindingAdapterPosition() == 0 ? View.INVISIBLE : View.VISIBLE)
                        .image(R.id.iv_dot, viewHolder.getBindingAdapterPosition() == 0 ? R.drawable.timeline_dot_blue : R.drawable.timeline_dot_gray);
            }
        });
        rv_task_log.setAdapter(mAdapter);

        setupContent();
    }

    @Override
    public void onDestroyView() {
        mDisposable.dispose();
        super.onDestroyView();
    }

    private void setupContent() {
        showProgress();
        mDisposable = RestAPI.getInstance().apiService().getTaskLogs(mTaskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResponseData -> {
                            hideProgress();
                            Collections.reverse(listResponseData.data);
                            mAdapter.updateData(listResponseData.data);
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }
}

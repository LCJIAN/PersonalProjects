package com.org.firefighting.ui.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.util.common.DateUtils;
import com.org.firefighting.GlideApp;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.TaskAnswer;
import com.org.firefighting.data.network.entity.TaskQuestion;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class QAFragment extends BaseFragment {

    private RecyclerView rv_qa;

    private SlimAdapter mAdapter;

    private String mTaskId;

    private Disposable mDisposable;
    private Disposable mDisposableR;

    public static QAFragment newInstance(String taskId) {
        QAFragment fragment = new QAFragment();
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
        View view = inflater.inflate(R.layout.fragment_task_qa, container, false);
        rv_qa = view.findViewById(R.id.rv_qa);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv_qa.setHasFixedSize(false);
        rv_qa.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<TaskQuestion>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.question_item;
            }

            @Override
            public void onBind(TaskQuestion data, SlimAdapter.SlimViewHolder<TaskQuestion> viewHolder) {
                viewHolder
                        .with(R.id.iv_question_user_avatar, v -> {
                            GlideApp.with(v)
                                    .load(data.id)
                                    .placeholder(R.drawable.default_avatar)
                                    .into((ImageView) v);
                        })
                        .text(R.id.tv_question_user_name, data.createByName)
                        .text(R.id.tv_question_user_name, data.departmentName + "-" + data.createByName)
                        .text(R.id.tv_question_time, DateUtils.convertDateToStr(new Date(data.createDate), DateUtils.YYYY_MM_DD_HH_MM_SS))
                        .text(R.id.tv_question_detail, data.question)
                        .with(R.id.rv_answer, v -> {
                            RecyclerView rv = (RecyclerView) v;
                            rv.setHasFixedSize(false);
                            rv.setLayoutManager(new LinearLayoutManager(v.getContext()));
                            rv.setAdapter(SlimAdapter.create().register(new SlimAdapter.SlimInjector<TaskAnswer>() {
                                @Override
                                public int onGetLayoutResource() {
                                    return R.layout.answer_item;
                                }

                                @Override
                                public void onBind(TaskAnswer answer, SlimAdapter.SlimViewHolder<TaskAnswer> vh) {
                                    vh
                                            .with(R.id.iv_answer_user_avatar, v -> {
                                                GlideApp.with(v)
                                                        .load(answer.id)
                                                        .placeholder(R.drawable.default_avatar)
                                                        .into((ImageView) v);
                                            })
                                            .text(R.id.tv_answer_detail, answer.answer)
                                            .text(R.id.tv_answer_user_name, answer.departmentName + "-" + answer.createByName)
                                            .text(R.id.tv_answer_time, DateUtils.convertDateToStr(new Date(answer.createDate), DateUtils.YYYY_MM_DD_HH_MM_SS));
                                }
                            }).updateData(data.answers));
                        });
            }
        });
        rv_qa.setAdapter(mAdapter);

        mDisposableR = RxBus.getInstance().asFlowable()
                .filter(o -> o instanceof TaskQuestion)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(q -> {
                    TaskQuestion tq = (TaskQuestion) q;
                    List<Object> l = new ArrayList<>();
                    l.add(tq);
                    if (mAdapter.getData() != null) {
                        l.addAll(mAdapter.getData());
                    }
                    mAdapter.updateData(l);
                });

        setupContent();
    }

    @Override
    public void onDestroyView() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposableR.dispose();
        super.onDestroyView();
    }

    private void setupContent() {
        showProgress();
        mDisposable = RestAPI.getInstance().apiService().getTaskQuestions(mTaskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResponseData -> {
                            hideProgress();
                            mAdapter.updateData(listResponseData.data);
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }
}

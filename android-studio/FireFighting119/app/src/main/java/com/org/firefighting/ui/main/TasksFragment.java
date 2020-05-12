package com.org.firefighting.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.common.DateUtils;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.Task;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.task.TaskDetailActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TasksFragment extends BaseFragment {

    @BindView(R.id.tab_task)
    TabLayout tab_task;
    @BindView(R.id.vp_task)
    ViewPager vp_task;

    private List<String> mTitlesO = Arrays.asList("待填报", "已填报", "已完结");
    private List<String> mTitles = new ArrayList<>(mTitlesO);

    private Unbinder mUnBinder;

    private Disposable mDisposable;

    private TaskPagerAdapter mPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frament_tasks, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mPagerAdapter = new TaskPagerAdapter(getChildFragmentManager(), mTitles);

        vp_task.setOffscreenPageLimit(3);
        vp_task.setAdapter(mPagerAdapter);
        tab_task.setupWithViewPager(vp_task);

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
    public void onDestroyView() {
        mDisposable.dispose();
        mUnBinder.unbind();
        super.onDestroyView();
    }

    private static class TaskPagerAdapter extends FragmentStatePagerAdapter {

        private List<String> mTitles;

        private TaskPagerAdapter(FragmentManager fm, List<String> titles) {
            super(fm);
            this.mTitles = titles;
        }

        @Override
        @NonNull
        public Fragment getItem(int position) {
            return TaskListFragment.newInstance(mTitles.get(position));
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

    public static class TaskListFragment extends Fragment {

        private SwipeRefreshLayout srl_task;
        private RecyclerView rv_task;

        private SlimAdapter mAdapter;
        private String mTabTitle;

        private Disposable mDisposable;

        private static TaskListFragment newInstance(String tabTitle) {
            TaskListFragment fragment = new TaskListFragment();
            fragment.mTabTitle = tabTitle;
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_task_list, container, false);
            srl_task = view.findViewById(R.id.srl_task);
            rv_task = view.findViewById(R.id.rv_task);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            srl_task.setColorSchemeResources(R.color.colorPrimary);
            srl_task.setOnRefreshListener(this::setupContent);

            mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<Task>() {
                @Override
                public int onGetLayoutResource() {
                    return R.layout.task_item;
                }

                @Override
                public void onInit(SlimAdapter.SlimViewHolder<Task> viewHolder) {
                    viewHolder.clicked(v -> v.getContext().startActivity(
                            new Intent(v.getContext(), TaskDetailActivity.class).putExtra("task_id", viewHolder.itemData.id)));
                }

                @Override
                public void onBind(Task data, SlimAdapter.SlimViewHolder<Task> viewHolder) {
                    viewHolder.text(R.id.tv_task_code, "任务:" + data.code)
                            .text(R.id.tv_task_end_date, "截至日期:" + data.endDate)
                            .text(R.id.tv_task_name, (TextUtils.equals("待查看", data.myWorkStatus.workStatus)
                                    ? new Spans().append("[未读]", new ForegroundColorSpan(0xffff0000)).append(" ").append(data.name)
                                    : data.name))
                            .text(R.id.tv_task_from, data.departmentName);

                    String f = "yyyy-MM-dd";
                    int days = (int)
                            ((DateUtils.convertStrToDate(data.endDate.split(" ")[0], f).getTime()
                                    - DateUtils.convertStrToDate(DateUtils.convertDateToStr(DateUtils.now(), f), f).getTime()) / (1000 * 3600 * 24));
                    if (days <= 0) {
                        viewHolder.text(R.id.tv_task_remaining, "超" + Math.abs(days) + "天")
                                .textColor(R.id.tv_task_remaining, 0xffff0000);
                    } else {
                        viewHolder.text(R.id.tv_task_remaining, "剩" + days + "天")
                                .textColor(R.id.tv_task_remaining, 0xff19d100);
                    }
                }
            });

            rv_task.setHasFixedSize(true);
            rv_task.setLayoutManager(new LinearLayoutManager(view.getContext()));
            rv_task.setAdapter(mAdapter);

            setupContent();
        }

        @Override
        public void onDestroyView() {
            if (mDisposable != null) {
                mDisposable.dispose();
            }
            super.onDestroyView();
        }

        private void setupContent() {
            srl_task.post(() -> srl_task.setRefreshing(true));
            if (mDisposable != null) {
                mDisposable.dispose();
            }
            mDisposable = RestAPI.getInstance().apiService().getMyTasks()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(pageResponse -> {
                        srl_task.post(() -> srl_task.setRefreshing(false));
                        List<Task> result = new ArrayList<>();
                        for (Task task : pageResponse.result) {
                            if (task.endDate == null) {
                                continue;
                            }
                            if (TextUtils.equals("待填报", mTabTitle)) {
                                if ((TextUtils.equals(task.myWorkStatus.workStatus, "待查看")
                                        || TextUtils.equals(task.myWorkStatus.workStatus, "填报中"))
                                        && !TextUtils.equals(task.status, "已完结")) {
                                    result.add(task);
                                }
                            } else if (TextUtils.equals("已填报", mTabTitle)) {
                                if (TextUtils.equals(task.myWorkStatus.workStatus, "已提交")
                                        && !TextUtils.equals(task.status, "已完结")) {
                                    result.add(task);
                                }
                            } else if (TextUtils.equals("已完结", mTabTitle)) {
                                if (TextUtils.equals(task.status, "已完结")) {
                                    result.add(task);
                                }
                            }
                        }
                        RxBus.getInstance().send(new TitleChangeEvent(mTabTitle, result.size()));
                        mAdapter.updateData(result);
                    }, throwable -> {
                        srl_task.post(() -> srl_task.setRefreshing(false));
                        ThrowableConsumerAdapter.accept(throwable);
                    });
        }
    }
}

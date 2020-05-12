package com.winside.lighting.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.data.network.entity.Project;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.base.SlimAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends BaseFragment {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.srl_project)
    SwipeRefreshLayout srl_project;
    @BindView(R.id.rv_project)
    RecyclerView rv_project;

    private Unbinder unbinder;

    private SlimAdapter mAdapter;

    private Disposable mDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_navigation_title.setText(R.string.action_home);
        srl_project.setColorSchemeResources(R.color.colorPrimary);
        srl_project.setOnRefreshListener(this::setupContent);
        rv_project.setHasFixedSize(true);
        rv_project.setLayoutManager(new LinearLayoutManager(rv_project.getContext()));

        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<Project>() {

            @Override
            public int onGetLayoutResource() {
                return R.layout.project_item;
            }

            @Override
            public void onBind(Project data, SlimAdapter.SlimViewHolder<Project> viewHolder) {
                Context context = viewHolder.itemView.getContext();
                viewHolder
                        .text(R.id.tv_project_name, data.name)
                        .text(R.id.tv_project_build_count, context.getString(R.string.project_build_count, data.buildCount))
                        .text(R.id.tv_total_light_count, context.getString(R.string.device_count, data.lightTotal == null ? 0 : data.lightTotal))
                        .text(R.id.tv_total_light_fixed_count, context.getString(R.string.device_count, data.lightFixedTotal == null ? 0 : data.lightFixedTotal))
                        .text(R.id.tv_total_light_online_count, context.getString(R.string.device_count, data.lightOnlineTotal == null ? 0 : data.lightOnlineTotal))
                        .text(R.id.tv_total_sensor_count, context.getString(R.string.device_count, data.sensorTotal == null ? 0 : data.sensorTotal))
                        .text(R.id.tv_total_sensor_fixed_count, context.getString(R.string.device_count, data.sensorFixedTotal == null ? 0 : data.sensorFixedTotal))
                        .text(R.id.tv_total_sensor_online_count, context.getString(R.string.device_count, data.sensorOnlineTotal == null ? 0 : data.sensorOnlineTotal));
            }
        });
        rv_project.setAdapter(mAdapter);

        setupContent();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mDisposable.dispose();
    }

    private void setupContent() {
        srl_project.post(() -> srl_project.setRefreshing(true));
        mDisposable = RestAPI.getInstance().lightingService().getProjects()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResponseData -> {
                            srl_project.post(() -> srl_project.setRefreshing(false));
                            if (listResponseData.code == 1000) {
                                mAdapter.updateData(listResponseData.data);
                                SharedPreferencesDataSource.setCurrentProjectId(listResponseData.data.get(0).id);
                            } else {
                                Toast.makeText(App.getInstance(), listResponseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            srl_project.post(() -> srl_project.setRefreshing(false));
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }
}

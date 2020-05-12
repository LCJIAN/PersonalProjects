package com.org.firefighting.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lcjian.lib.recyclerview.SlimAdapter;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.Department;
import com.org.firefighting.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DepartmentsActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.srl_department)
    SwipeRefreshLayout srl_department;
    @BindView(R.id.rv_department)
    RecyclerView rv_department;

    private SlimAdapter mAdapter;

    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departments);
        ButterKnife.bind(this);

        btn_back.setOnClickListener(v -> onBackPressed());

        srl_department.setColorSchemeResources(R.color.colorPrimary);
        srl_department.setOnRefreshListener(this::setupContent);

        rv_department.setHasFixedSize(true);
        rv_department.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<Department>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.department_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<Department> viewHolder) {
                viewHolder.clicked(v -> startActivity(new Intent(v.getContext(), DepartmentUsersActivity.class)
                        .putExtra("dept_code", viewHolder.itemData.code)
                        .putExtra("dept_name", viewHolder.itemData.name))
                );
            }

            @Override
            public void onBind(Department data, SlimAdapter.SlimViewHolder<Department> viewHolder) {
                viewHolder.text(R.id.tv_department_name, data.name);
            }
        });
        rv_department.setAdapter(mAdapter);

        setupContent();
    }

    private void setupContent() {
        setRefreshing(true);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = RestAPI.getInstance().apiServiceC().getDepartments()
                .flatMapObservable(departments -> Observable.fromIterable(departments.result))
                .flatMap(department -> Observable.fromIterable(department.children))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                            setRefreshing(false);
                            mAdapter.updateData(list);
                        },
                        throwable -> {
                            ThrowableConsumerAdapter.accept(throwable);
                            setRefreshing(false);
                        });
    }

    private void setRefreshing(final boolean refreshing) {
        if (srl_department.isEnabled()) {
            srl_department.post(() -> {
                if (srl_department != null) {
                    srl_department.setRefreshing(refreshing);
                }
            });
        }
    }

}

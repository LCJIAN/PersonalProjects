package com.org.firefighting.ui.service;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.ServiceEntity;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.ui.common.ApplyFragment;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ServiceDetailActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;
    @BindView(R.id.btn_apply)
    AppCompatButton btn_apply;

    private String mServiceId;
    private ServiceEntity mServiceEntity;
    private Disposable mDisposable;
    private Disposable mDisposableA;
    private Disposable mDisposableR;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);
        ButterKnife.bind(this);
        mServiceId = getIntent().getStringExtra("service_id");

        tv_title.setText("服务详情");
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.favourite_btn);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_apply.setOnClickListener(v -> RxBus.getInstance().send(new ShowApplyDialogEvent()));
        btn_nav_right.setOnClickListener(v -> {
            if (mServiceEntity != null) {
                if (mServiceEntity.collectStatus == 0) {
                    favourite();
                } else {
                    unFavourite();
                }
            }
        });
        mDisposableR = RxBus.getInstance()
                .asFlowable()
                .filter(o -> o instanceof ShowApplyDialogEvent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> ApplyFragment
                        .newInstance("服务名称：" + mServiceEntity.serviceName
                                + "\n服务描述：" + (TextUtils.isEmpty(mServiceEntity.description) ? "暂无" : mServiceEntity.description))
                        .setListener(this::applyService)
                        .show(getSupportFragmentManager(), "ApplyFragment"));

        setupContent();
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableA != null) {
            mDisposableA.dispose();
        }
        if (mDisposableR != null) {
            mDisposableR.dispose();
        }
        super.onDestroy();
    }

    private void setupContent() {
        showProgress();
        mDisposable = RestAPI.getInstance().apiServiceSB()
                .getService(mServiceId, SharedPreferencesDataSource.getSignInResponse().user.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            mServiceEntity = responseData.data;

                            if (mServiceEntity.collectStatus == 0) {
                                btn_nav_right.setImageResource(R.drawable.favourite_btn);
                            } else {
                                btn_nav_right.setImageResource(R.drawable.un_favourite_btn);
                            }
                            if (2 == mServiceEntity.applyStatus) { // 审核通过，有权限
                                if (getSupportFragmentManager().findFragmentByTag("ServiceBasicInfoFragment") == null) {
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fl_fragment_container_basic, ServiceBasicInfoFragment.newInstance(mServiceEntity), "ServiceBasicInfoFragment")
                                            .replace(R.id.fl_fragment_container_query, DataQueryFragment.newInstance(mServiceEntity.invokeName, mServiceEntity), "DataQueryFragment")
                                            .commit();
                                }
                            } else {
                                if (getSupportFragmentManager().findFragmentByTag("ServiceBasicInfoFragment") == null) {
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fl_fragment_container_basic, ServiceBasicInfoFragment.newInstance(mServiceEntity), "ServiceBasicInfoFragment")
                                            .replace(R.id.fl_fragment_container_query, DataFieldFragment.newInstance(mServiceEntity), "DataFieldFragment")
                                            .commit();
                                }
                            }
                            if (1 == mServiceEntity.applyStatus) {
                                btn_apply.setVisibility(View.GONE);
                            } else if (2 == mServiceEntity.applyStatus) {
                                btn_apply.setVisibility(View.GONE);
                            } else if (3 == mServiceEntity.applyStatus) {
                                btn_apply.setVisibility(View.VISIBLE);
                                btn_apply.setText("重新申请");
                            } else {
                                btn_apply.setVisibility(View.VISIBLE);
                                btn_apply.setText("立即申请");
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void favourite() {
        showProgress();
        if (mDisposableA != null) {
            mDisposableA.dispose();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", SharedPreferencesDataSource.getSignInResponse().user.id);
        map.put("category", "service");
        map.put("userType", "external");
        mDisposableA = RestAPI.getInstance().apiServiceSB()
                .favouriteService(mServiceEntity.id, map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_SHORT).show();
                            if (responseData.code == 0) {
                                setupContent();
                                RxBus.getInstance().send(new ServiceListActivity.RefreshServicesEvent());
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void unFavourite() {
        showProgress();
        if (mDisposableA != null) {
            mDisposableA.dispose();
        }
        mDisposableA = RestAPI.getInstance().apiServiceSB()
                .unFavouriteService(mServiceEntity.collectId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_SHORT).show();
                            if (responseData.code == 0) {
                                setupContent();
                                RxBus.getInstance().send(new ServiceListActivity.RefreshServicesEvent());
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void applyService(String name, String reason) {
        if (mServiceEntity == null) {
            return;
        }
        showProgress();
        if (mDisposableA != null) {
            mDisposableA.dispose();
        }
        Map<String, String> map = new HashMap<>();
        map.put("name", reason);
        map.put("category", "service");
        map.put("applyReason", reason);
        map.put("relationId", mServiceEntity.id);
        mDisposableA = RestAPI.getInstance().apiServiceSB()
                .applyService(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_SHORT).show();
                            if (responseData.code == 0) {
                                setupContent();
                                RxBus.getInstance().send(new ServiceListActivity.RefreshServicesEvent());
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    static class ShowApplyDialogEvent {
    }
}

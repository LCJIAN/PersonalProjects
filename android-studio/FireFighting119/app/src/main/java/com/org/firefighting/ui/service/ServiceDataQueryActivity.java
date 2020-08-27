package com.org.firefighting.ui.service;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

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

public class ServiceDataQueryActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;

    @BindView(R.id.fl_apply)
    CardView fl_apply;
    @BindView(R.id.tv_apply)
    TextView tv_apply;

    private String mServiceId;
    private ServiceEntity mServiceEntity;
    private PermissionEvent mPermissionEvent;
    private Disposable mDisposable;
    private Disposable mDisposableA;
    private Disposable mDisposableR;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_data_query);
        ButterKnife.bind(this);
        mServiceId = getIntent().getStringExtra("service_id");
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.favourite_btn);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_nav_right.setOnClickListener(v -> {
            if (mServiceEntity != null) {
                if (mServiceEntity.collectStatus == 0) {
                    favourite();
                } else {
                    unFavourite();
                }
            }
        });
        fl_apply.setOnClickListener(v -> {
            if (mServiceEntity != null) {
                if (1 != mServiceEntity.applyStatus
                        && 2 != mServiceEntity.applyStatus) {
                    ApplyFragment
                            .newInstance("服务名称：" + mServiceEntity.serviceName
                                    + "\n服务描述：" + (TextUtils.isEmpty(mServiceEntity.description) ? "暂无" : mServiceEntity.description))
                            .setService(true)
                            .setListener(this::applyService)
                            .show(getSupportFragmentManager(), "ApplyFragment");
                }
            }
        });

        getService();

        mDisposableR = RxBus.getInstance()
                .asFlowable()
                .filter(o -> o instanceof PermissionEvent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    mPermissionEvent = (PermissionEvent) o;
                    setupButtons();
                });
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

    private void getService() {
        showProgress();
        mDisposable = RestAPI.getInstance().apiServiceSB()
                .getService(mServiceId, SharedPreferencesDataSource.getSignInResponse().user.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            mServiceEntity = responseData.data;
                            setupAll();
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void setupAll() {
        tv_title.setText(mServiceEntity.serviceName);
        setupContent();
        setupButtons();
    }

    private void setupContent() {
        if (getSupportFragmentManager().findFragmentByTag("DataQueryFragment") == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_fragment_container, DataQueryFragment.newInstance(mServiceEntity.invokeName), "DataQueryFragment")
                    .commit();
        }
    }

    private void setupButtons() {
        if (mPermissionEvent == null || mServiceEntity == null) {
            return;
        }
        if (mServiceEntity.collectStatus == 0) {
            btn_nav_right.setImageResource(R.drawable.favourite_btn);
        } else {
            btn_nav_right.setImageResource(R.drawable.un_favourite_btn);
        }
        if (mPermissionEvent.permission) {
            fl_apply.setVisibility(View.GONE);
        } else {
            fl_apply.setVisibility(View.VISIBLE);
            if (1 == mServiceEntity.applyStatus) {
                tv_apply.setText("已申请");
                tv_apply.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_security_white_48dp, 0, 0, 0);
            } else {
                tv_apply.setText("申请");
                tv_apply.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_security_check_white_48dp, 0, 0, 0);
            }
        }
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
                                getService();
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
                                getService();
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
                                mServiceEntity.applyStatus = 1;
                                setupButtons();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    static class PermissionEvent {
        boolean permission;

        PermissionEvent(boolean permission) {
            this.permission = permission;
        }
    }
}
